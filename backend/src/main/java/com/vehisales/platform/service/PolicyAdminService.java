package com.vehisales.platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehisales.platform.api.dto.DealerOfferResponse;
import com.vehisales.platform.api.dto.admin.DealerPolicyRecord;
import com.vehisales.platform.api.dto.admin.FeePolicyRecord;
import com.vehisales.platform.api.dto.admin.PlateRegionsRecord;
import com.vehisales.platform.config.DealerPolicyProperties;
import com.vehisales.platform.config.FeePolicyProperties;
import com.vehisales.platform.config.LicensePlateRegionsProperties;
import com.vehisales.platform.domain.AppSetting;
import com.vehisales.platform.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PolicyAdminService {

    static final String FEE_POLICY = "fee-policy";
    static final String DEALER_POLICY = "dealer-policy";
    static final String PLATE_REGIONS = "license-plate-regions";

    private final FeePolicyProperties feePolicyProperties;
    private final DealerPolicyProperties dealerPolicyProperties;
    private final LicensePlateRegionsProperties plateRegionsProperties;
    private final AppSettingRepository settings;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final TextTranslateService textTranslateService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadSavedPolicies() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS app_settings (
                    setting_key VARCHAR(80) PRIMARY KEY,
                    payload TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS color_photos TEXT");
        jdbcTemplate.update("""
                DELETE FROM fee_rules
                WHERE fee_definition_id IN (
                    SELECT id FROM fee_definitions WHERE code IN ('LICENSE_PLATE', 'REGISTRATION_TAX')
                )
                """);
        settings.findById(FEE_POLICY).ifPresent(row -> applyFee(read(row.getPayload(), FeePolicyRecord.class)));
        settings.findById(DEALER_POLICY).ifPresent(row -> applyDealer(read(row.getPayload(), DealerPolicyRecord.class)));
        settings.findById(PLATE_REGIONS).ifPresent(row -> applyPlates(read(row.getPayload(), PlateRegionsRecord.class)));
    }

    public FeePolicyRecord getFeePolicy() {
        return new FeePolicyRecord(
                feePolicyProperties.getRegistrationTaxPercent(),
                feePolicyProperties.getRegistrationTaxCommercialPercent()
        );
    }

    @Transactional
    public FeePolicyRecord saveFeePolicy(FeePolicyRecord record) {
        FeePolicyRecord next = new FeePolicyRecord(
                requiredAmount(record == null ? null : record.registrationTaxPercent(), "Thuế trước bạ xe cá nhân"),
                requiredAmount(record == null ? null : record.registrationTaxCommercialPercent(), "Thuế trước bạ xe kinh doanh")
        );
        applyFee(next);
        persist(FEE_POLICY, next);
        return next;
    }

    public DealerPolicyRecord getDealerPolicy() {
        return toDealerRecord();
    }

    @Transactional
    public DealerPolicyRecord saveDealerPolicy(DealerPolicyRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Dealer policy is required");
        }
        applyDealer(record);
        persist(DEALER_POLICY, toDealerRecord());
        return toDealerRecord();
    }

    public PlateRegionsRecord getPlateRegions() {
        return toPlateRecord();
    }

    @Transactional
    public PlateRegionsRecord savePlateRegions(PlateRegionsRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("License plate regions are required");
        }
        applyPlates(record);
        persist(PLATE_REGIONS, toPlateRecord());
        return toPlateRecord();
    }

    private void applyFee(FeePolicyRecord record) {
        feePolicyProperties.setRegistrationTaxPercent(record.registrationTaxPercent());
        feePolicyProperties.setRegistrationTaxCommercialPercent(record.registrationTaxCommercialPercent());
    }

    private void applyDealer(DealerPolicyRecord record) {
        if (dealerPolicyProperties.getDiscount() == null) {
            dealerPolicyProperties.setDiscount(new DealerPolicyProperties.Discount());
        }
        dealerPolicyProperties.getDiscount().setPrivatePercent(zeroIfNull(record.privateDiscountPercent()));
        dealerPolicyProperties.getDiscount().setCommercialPercent(zeroIfNull(record.commercialDiscountPercent()));
        List<DealerPolicyProperties.Offer> offers = new ArrayList<>();
        if (record.offers() != null) {
            for (DealerOfferResponse item : record.offers()) {
                if (item == null || item.title() == null || item.title().isEmpty()) {
                    continue;
                }
                DealerPolicyProperties.Offer offer = new DealerPolicyProperties.Offer();
                offer.setId(item.id() == null || item.id().isBlank() ? slug(firstTitle(item.title())) : item.id());
                offer.setKind(item.kind() == null || item.kind().isBlank() ? DealerPolicy.FORGO_FOR_CREDIT : item.kind());
                offer.setAmount(zeroIfNull(item.amount()));
                offer.setPercent(zeroIfNull(item.percent()));
                offer.setTitle(textTranslateService.completeMap(item.title()));
                offer.setDescription(textTranslateService.completeMap(item.description()));
                offers.add(offer);
            }
        }
        dealerPolicyProperties.setOffers(offers);
    }

    private void applyPlates(PlateRegionsRecord record) {
        plateRegionsProperties.setDefaultArea(record.defaultArea() == null || record.defaultArea().isBlank()
                ? "AREA_II"
                : record.defaultArea());
        Map<String, LicensePlateRegionsProperties.AreaRate> areas = new LinkedHashMap<>();
        if (record.areas() != null) {
            record.areas().forEach((code, area) -> {
                LicensePlateRegionsProperties.AreaRate rate = new LicensePlateRegionsProperties.AreaRate();
                rate.setAmount(area == null || area.amount() == null ? BigDecimal.ZERO : area.amount());
                areas.put(code, rate);
            });
        }
        plateRegionsProperties.setAreas(areas);
        Map<String, List<LicensePlateRegionsProperties.Unit>> regions = new LinkedHashMap<>();
        if (record.regions() != null) {
            record.regions().forEach((region, units) -> {
                List<LicensePlateRegionsProperties.Unit> next = new ArrayList<>();
                if (units != null) {
                    for (PlateRegionsRecord.Unit unit : units) {
                        if (unit == null || unit.name() == null || unit.name().isBlank()) {
                            continue;
                        }
                        LicensePlateRegionsProperties.Unit item = new LicensePlateRegionsProperties.Unit();
                        item.setName(unit.name().trim());
                        item.setCode(unit.code() == null || unit.code().isBlank() ? slug(unit.name()) : unit.code().trim());
                        item.setArea(unit.area() == null || unit.area().isBlank() ? plateRegionsProperties.getDefaultArea() : unit.area());
                        next.add(item);
                    }
                }
                regions.put(region, next);
            });
        }
        plateRegionsProperties.setRegions(regions);
    }

    private DealerPolicyRecord toDealerRecord() {
        DealerPolicyProperties.Discount discount = dealerPolicyProperties.getDiscount();
        List<DealerOfferResponse> offers = new ArrayList<>();
        for (DealerPolicyProperties.Offer offer : dealerPolicyProperties.getOffers()) {
            offers.add(new DealerOfferResponse(
                    offer.getId(),
                    offer.getKind(),
                    offer.getAmount(),
                    offer.getPercent(),
                    offer.getTitle(),
                    offer.getDescription()
            ));
        }
        return new DealerPolicyRecord(
                discount == null ? BigDecimal.ZERO : discount.getPrivatePercent(),
                discount == null ? BigDecimal.ZERO : discount.getCommercialPercent(),
                offers
        );
    }

    private PlateRegionsRecord toPlateRecord() {
        Map<String, PlateRegionsRecord.Area> areas = new LinkedHashMap<>();
        plateRegionsProperties.getAreas().forEach((code, rate) ->
                areas.put(code, new PlateRegionsRecord.Area(rate.getAmount())));
        Map<String, List<PlateRegionsRecord.Unit>> regions = new LinkedHashMap<>();
        plateRegionsProperties.getRegions().forEach((region, units) ->
                regions.put(region, units.stream()
                        .map(unit -> new PlateRegionsRecord.Unit(unit.getCode(), unit.getName(), unit.getArea()))
                        .toList()));
        return new PlateRegionsRecord(plateRegionsProperties.getDefaultArea(), areas, regions);
    }

    private void persist(String key, Object value) {
        try {
            settings.save(new AppSetting(key, objectMapper.writeValueAsString(value)));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Could not save " + key);
        }
    }

    private <T> T read(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not read saved " + type.getSimpleName());
        }
    }

    private static BigDecimal requiredAmount(BigDecimal value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String firstTitle(Map<String, String> title) {
        if (title.containsKey("vi") && title.get("vi") != null && !title.get("vi").isBlank()) {
            return title.get("vi");
        }
        return title.values().stream().filter(item -> item != null && !item.isBlank()).findFirst().orElse("offer");
    }

    static String slug(String name) {
        String ascii = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase(java.util.Locale.ROOT);
        if (ascii.isBlank()) {
            return "ITEM";
        }
        return ascii.length() > 32 ? ascii.substring(0, 32) : ascii;
    }
}
