package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.admin.BrandRecord;
import com.vehisales.platform.api.dto.admin.CatalogSnapshot;
import com.vehisales.platform.api.dto.admin.CategoryRecord;
import com.vehisales.platform.api.dto.admin.DealerRecord;
import com.vehisales.platform.api.dto.admin.FeeDefinitionRecord;
import com.vehisales.platform.api.dto.admin.FeeRuleRecord;
import com.vehisales.platform.api.dto.admin.ImportResult;
import com.vehisales.platform.api.dto.admin.LocationRecord;
import com.vehisales.platform.api.dto.admin.VehicleRecord;
import com.vehisales.platform.domain.Brand;
import com.vehisales.platform.domain.Dealer;
import com.vehisales.platform.domain.FeeDefinition;
import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import com.vehisales.platform.domain.enums.FeeCalculationType;
import com.vehisales.platform.domain.enums.FeeZone;
import com.vehisales.platform.domain.enums.Region;
import com.vehisales.platform.exception.ResourceNotFoundException;
import com.vehisales.platform.repository.BrandRepository;
import com.vehisales.platform.repository.DealerRepository;
import com.vehisales.platform.repository.FeeDefinitionRepository;
import com.vehisales.platform.repository.FeeRuleRepository;
import com.vehisales.platform.repository.LocationRepository;
import com.vehisales.platform.repository.VehicleCategoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogAdminService {

    private final BrandRepository brandRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final DealerRepository dealerRepository;
    private final FeeDefinitionRepository feeDefinitionRepository;
    private final VehicleRepository vehicleRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final TextTranslateService textTranslateService;

    @Transactional(readOnly = true)
    public CatalogSnapshot exportAll() {
        return new CatalogSnapshot(
                brandRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList(),
                categoryRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList(),
                locationRepository.findAllByOrderByNameAsc().stream().map(this::toRecord).toList(),
                dealerRepository.findAllByOrderByNameAsc().stream().map(this::toRecord).toList(),
                feeDefinitionRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList(),
                vehicleRepository.findAllDetailedByOrderByNameAsc().stream().map(this::toRecord).toList(),
                feeRuleRepository.findAllDetailedByOrderByIdAsc().stream().map(this::toRecord).toList()
        );
    }

    @Transactional
    public ImportResult importAll(CatalogSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Import body is required");
        }
        int brands = applyAll(snapshot.brands(), this::upsertBrand);
        int categories = applyAll(snapshot.categories(), this::upsertCategory);
        int locations = applyAll(snapshot.locations(), this::upsertLocation);
        int feeDefinitions = applyAll(snapshot.feeDefinitions(), this::upsertFeeDefinition);
        int dealers = applyAll(snapshot.dealers(), this::upsertDealer);
        int vehicles = applyAll(snapshot.vehicles(), this::upsertVehicle);
        int feeRules = applyAll(snapshot.feeRules(), this::upsertFeeRule);
        return new ImportResult(brands, categories, locations, dealers, feeDefinitions, vehicles, feeRules);
    }

    @Transactional(readOnly = true)
    public List<BrandRecord> listBrands() {
        return brandRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public BrandRecord upsertBrand(BrandRecord record) {
        Brand entity = resolveBrand(record);
        apply(entity, record);
        return toRecord(brandRepository.save(entity));
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
        if (vehicleRepository.existsByBrand_Id(id) || dealerRepository.existsByBrand_Id(id)) {
            throw new IllegalArgumentException("Cannot delete brand " + brand.getCode() + " while vehicles or dealers still reference it");
        }
        brandRepository.delete(brand);
    }

    @Transactional(readOnly = true)
    public List<CategoryRecord> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public CategoryRecord upsertCategory(CategoryRecord record) {
        VehicleCategory entity = resolveCategory(record);
        apply(entity, record);
        return toRecord(categoryRepository.save(entity));
    }

    @Transactional
    public void deleteCategory(Long id) {
        VehicleCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (vehicleRepository.existsByCategory_Id(id) || feeRuleRepository.existsByCategory_Id(id)) {
            throw new IllegalArgumentException("Cannot delete category " + category.getCode() + " while vehicles or fee rules still reference it");
        }
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<LocationRecord> listLocations() {
        return locationRepository.findAllByOrderByNameAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public LocationRecord upsertLocation(LocationRecord record) {
        Location entity = resolveLocation(record);
        apply(entity, record);
        return toRecord(locationRepository.save(entity));
    }

    @Transactional
    public void deleteLocation(Long id) {
        Location location = locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location", id));
        if (feeRuleRepository.existsByLocation_Id(id)) {
            throw new IllegalArgumentException("Cannot delete location " + location.getCode() + " while fee rules still reference it");
        }
        locationRepository.delete(location);
    }

    @Transactional(readOnly = true)
    public List<DealerRecord> listDealers() {
        return dealerRepository.findAllByOrderByNameAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public DealerRecord upsertDealer(DealerRecord record) {
        Dealer entity = resolveDealer(record);
        apply(entity, record);
        return toRecord(dealerRepository.save(entity));
    }

    @Transactional
    public void deleteDealer(Long id) {
        if (!dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer", id);
        }
        dealerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FeeDefinitionRecord> listFeeDefinitions() {
        return feeDefinitionRepository.findAllByOrderBySortOrderAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public FeeDefinitionRecord upsertFeeDefinition(FeeDefinitionRecord record) {
        FeeDefinition entity = resolveFeeDefinition(record);
        apply(entity, record);
        return toRecord(feeDefinitionRepository.save(entity));
    }

    @Transactional
    public void deleteFeeDefinition(Long id) {
        FeeDefinition definition = feeDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee definition", id));
        if (feeRuleRepository.existsByFeeDefinition_Id(id)) {
            throw new IllegalArgumentException("Cannot delete fee " + definition.getCode() + " while fee rules still reference it");
        }
        feeDefinitionRepository.delete(definition);
    }

    @Transactional(readOnly = true)
    public List<VehicleRecord> listVehicles() {
        return vehicleRepository.findAllDetailedByOrderByNameAsc().stream().map(this::toRecord).toList();
    }

    @Transactional
    public VehicleRecord upsertVehicle(VehicleRecord record) {
        Vehicle entity = resolveVehicle(record);
        apply(entity, record);
        return toRecord(vehicleRepository.save(entity));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle", id);
        }
        vehicleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FeeRuleRecord> listFeeRules() {
        return feeRuleRepository.findAllDetailedByOrderByIdAsc().stream()
                .filter(rule -> rule.getFeeDefinition() == null || !FeePolicy.isPolicyOwned(rule.getFeeDefinition().getCode()))
                .map(this::toRecord)
                .toList();
    }

    @Transactional
    public FeeRuleRecord upsertFeeRule(FeeRuleRecord record) {
        if (record != null && FeePolicy.isPolicyOwned(record.feeDefinitionCode())) {
            throw new IllegalArgumentException("License plate and registration tax are edited on their own tabs, not in fee rules");
        }
        FeeRule entity = resolveFeeRule(record);
        apply(entity, record);
        return toRecord(feeRuleRepository.save(entity));
    }

    @Transactional
    public void deleteFeeRule(Long id) {
        if (!feeRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fee rule", id);
        }
        feeRuleRepository.deleteById(id);
    }

    private Brand resolveBrand(BrandRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Brand is required");
        }
        if (record.id() != null) {
            return brandRepository.findById(record.id()).orElseThrow(() -> new ResourceNotFoundException("Brand", record.id()));
        }
        if (hasText(record.code())) {
            return brandRepository.findByCodeIgnoreCase(record.code()).orElseGet(Brand::new);
        }
        return new Brand();
    }

    private void apply(Brand entity, BrandRecord record) {
        requireText(record.name(), "Brand name");
        entity.setCode(codeOrSlug(record.code(), record.name()));
        entity.setName(record.name().trim());
        entity.setTagline(trimToNull(record.tagline()));
        entity.setMarket(hasText(record.market()) ? record.market().trim() : "Vietnam");
        entity.setAccentColor(trimToNull(record.accentColor()));
        entity.setImageUrl(trimToNull(record.imageUrl()));
        entity.setReady(record.ready() == null || record.ready());
        entity.setSortOrder(record.sortOrder() == null ? 0 : record.sortOrder());
    }

    private VehicleCategory resolveCategory(CategoryRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (record.id() != null) {
            return categoryRepository.findById(record.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", record.id()));
        }
        if (hasText(record.code())) {
            return categoryRepository.findByCodeIgnoreCase(record.code()).orElseGet(VehicleCategory::new);
        }
        return new VehicleCategory();
    }

    private void apply(VehicleCategory entity, CategoryRecord record) {
        requireText(record.name(), "Category name");
        entity.setCode(codeOrSlug(record.code(), record.name()));
        entity.setName(record.name().trim());
        entity.setDescription(trimToNull(record.description()));
        entity.setTypicalSeats(record.typicalSeats());
        entity.setRequiresInspection(Boolean.TRUE.equals(record.requiresInspection()));
        entity.setRequiresRoadUseFee(Boolean.TRUE.equals(record.requiresRoadUseFee()));
        entity.setRequiresCompulsoryInsurance(Boolean.TRUE.equals(record.requiresCompulsoryInsurance()));
        entity.setSortOrder(record.sortOrder() == null ? 0 : record.sortOrder());
    }

    private Location resolveLocation(LocationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Location is required");
        }
        if (record.id() != null) {
            return locationRepository.findById(record.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", record.id()));
        }
        if (hasText(record.code())) {
            return locationRepository.findByCodeIgnoreCase(record.code()).orElseGet(Location::new);
        }
        return new Location();
    }

    private void apply(Location entity, LocationRecord record) {
        requireText(record.name(), "Location name");
        var names = textTranslateService.fillIfNeeded(record.name(), record.nameEn(), record.nameZh(), record.nameJa());
        entity.setCode(codeOrSlug(record.code(), names.vi()));
        entity.setName(names.vi());
        entity.setNameEn(names.en());
        entity.setNameZh(names.zh());
        entity.setNameJa(names.ja());
        entity.setRegion(enumValue(Region.class, record.region(), "region"));
        entity.setFeeZone(enumValue(FeeZone.class, record.feeZone(), "feeZone"));
        entity.setCentrallyGovernedCity(Boolean.TRUE.equals(record.centrallyGovernedCity()));
    }

    private Dealer resolveDealer(DealerRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Dealer is required");
        }
        if (record.id() != null) {
            return dealerRepository.findById(record.id()).orElseThrow(() -> new ResourceNotFoundException("Dealer", record.id()));
        }
        if (hasText(record.brandCode()) && hasText(record.name())) {
            return dealerRepository.findByBrand_CodeIgnoreCaseAndNameIgnoreCase(record.brandCode(), record.name())
                    .orElseGet(Dealer::new);
        }
        return new Dealer();
    }

    private void apply(Dealer entity, DealerRecord record) {
        requireText(record.brandCode(), "Dealer brandCode");
        requireText(record.name(), "Dealer name");
        entity.setBrand(requireBrand(record.brandCode()));
        entity.setName(record.name().trim());
        entity.setAddress(trimToNull(record.address()));
        entity.setMarket(hasText(record.market()) ? record.market().trim() : "Vietnam");
        entity.setActive(record.active() == null || record.active());
    }

    private FeeDefinition resolveFeeDefinition(FeeDefinitionRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Fee definition is required");
        }
        if (record.id() != null) {
            return feeDefinitionRepository.findById(record.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Fee definition", record.id()));
        }
        if (hasText(record.code())) {
            return feeDefinitionRepository.findByCodeIgnoreCase(record.code()).orElseGet(FeeDefinition::new);
        }
        return new FeeDefinition();
    }

    private void apply(FeeDefinition entity, FeeDefinitionRecord record) {
        requireText(record.name(), "Fee name");
        entity.setCode(codeOrSlug(record.code(), record.name()));
        entity.setName(record.name().trim());
        entity.setDescription(trimToNull(record.description()));
        entity.setMandatory(record.mandatory() == null || record.mandatory());
        entity.setSortOrder(record.sortOrder() == null ? 0 : record.sortOrder());
        entity.setActive(record.active() == null || record.active());
    }

    private Vehicle resolveVehicle(VehicleRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Vehicle is required");
        }
        if (record.id() != null) {
            return vehicleRepository.findDetailedById(record.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", record.id()));
        }
        if (hasText(record.brandCode()) && hasText(record.model()) && hasText(record.name())) {
            return vehicleRepository
                    .findByBrand_CodeIgnoreCaseAndModelIgnoreCaseAndNameIgnoreCase(record.brandCode(), record.model(), record.name())
                    .orElseGet(Vehicle::new);
        }
        return new Vehicle();
    }

    private void apply(Vehicle entity, VehicleRecord record) {
        requireText(record.brandCode(), "Vehicle brandCode");
        requireText(record.categoryCode(), "Vehicle categoryCode");
        requireText(record.model(), "Vehicle model");
        requireText(record.name(), "Vehicle name");
        if (record.listPrice() == null) {
            throw new IllegalArgumentException("Vehicle listPrice is required");
        }
        entity.setBrand(requireBrand(record.brandCode()));
        entity.setCategory(requireCategory(record.categoryCode()));
        entity.setModel(record.model().trim());
        entity.setName(record.name().trim());
        entity.setSeats(record.seats());
        entity.setVehicleType(trimToNull(record.vehicleType()));
        entity.setYear(record.year());
        entity.setEngineCc(record.engineCc());
        entity.setFuelType(trimToNull(record.fuelType()));
        entity.setTransmission(trimToNull(record.transmission()));
        entity.setListPrice(record.listPrice());
        entity.setDiscountAmount(record.discountAmount());
        entity.setSalePrice(record.salePrice());
        entity.setTaxBasePrice(record.taxBasePrice());
        entity.setDefaultDeposit(record.defaultDeposit());
        entity.setRegistrationServiceFee(record.registrationServiceFee());
        entity.setMicaPlateFee(record.micaPlateFee());
        entity.setInspectionFee(record.inspectionFee());
        Map<String, String> photos = record.colorPhotos() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(record.colorPhotos());
        photos.entrySet().removeIf(entry -> !hasText(entry.getKey()) || !hasText(entry.getValue()));
        String colorList = trimToNull(record.availableColors());
        if (colorList == null && !photos.isEmpty()) {
            colorList = String.join(", ", photos.keySet());
        }
        String defaultColor = trimToNull(record.defaultColor());
        if (defaultColor == null && !photos.isEmpty()) {
            defaultColor = photos.keySet().iterator().next();
        }
        entity.setDefaultColor(defaultColor);
        entity.setAvailableColors(colorList);
        entity.setColorPhotos(photos);
        entity.setDeliveryNote(trimToNull(record.deliveryNote()));
        entity.setWarrantyNote(trimToNull(record.warrantyNote()));
        entity.setGifts(trimToNull(record.gifts()));
        entity.setQuoteSheetName(trimToNull(record.quoteSheetName()));
        entity.setImageUrl(trimToNull(record.imageUrl()));
        entity.setSpecifications(record.specifications() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(record.specifications()));
        entity.setActive(record.active() == null || record.active());
    }

    private FeeRule resolveFeeRule(FeeRuleRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Fee rule is required");
        }
        if (record.id() != null) {
            return feeRuleRepository.findDetailedById(record.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Fee rule", record.id()));
        }
        return new FeeRule();
    }

    private void apply(FeeRule entity, FeeRuleRecord record) {
        requireText(record.feeDefinitionCode(), "Fee rule feeDefinitionCode");
        requireText(record.calculationType(), "Fee rule calculationType");
        entity.setFeeDefinition(requireFeeDefinition(record.feeDefinitionCode()));
        entity.setCategory(hasText(record.categoryCode()) ? requireCategory(record.categoryCode()) : null);
        entity.setLocation(hasText(record.locationCode()) ? requireLocation(record.locationCode()) : null);
        entity.setFeeZone(optionalEnum(FeeZone.class, record.feeZone(), "feeZone"));
        entity.setCalculationType(enumValue(FeeCalculationType.class, record.calculationType(), "calculationType"));
        entity.setFixedAmount(record.fixedAmount());
        entity.setPercentage(record.percentage());
        entity.setMinAmount(record.minAmount());
        entity.setMaxAmount(record.maxAmount());
        entity.setMinEngineCc(record.minEngineCc());
        entity.setMaxEngineCc(record.maxEngineCc());
        entity.setMinPrice(record.minPrice());
        entity.setMaxPrice(record.maxPrice());
        entity.setPriority(record.priority() == null ? 0 : record.priority());
        entity.setEffectiveFrom(record.effectiveFrom());
        entity.setEffectiveTo(record.effectiveTo());
        entity.setActive(record.active() == null || record.active());
    }

    private BrandRecord toRecord(Brand brand) {
        return new BrandRecord(
                brand.getId(),
                brand.getCode(),
                brand.getName(),
                brand.getTagline(),
                brand.getMarket(),
                brand.getAccentColor(),
                brand.getImageUrl(),
                brand.isReady(),
                brand.getSortOrder()
        );
    }

    private CategoryRecord toRecord(VehicleCategory category) {
        return new CategoryRecord(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getTypicalSeats(),
                category.isRequiresInspection(),
                category.isRequiresRoadUseFee(),
                category.isRequiresCompulsoryInsurance(),
                category.getSortOrder()
        );
    }

    private LocationRecord toRecord(Location location) {
        return new LocationRecord(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getNameEn(),
                location.getNameZh(),
                location.getNameJa(),
                location.getRegion().name(),
                location.getFeeZone().name(),
                location.isCentrallyGovernedCity()
        );
    }

    private DealerRecord toRecord(Dealer dealer) {
        return new DealerRecord(
                dealer.getId(),
                dealer.getBrand().getCode(),
                dealer.getName(),
                dealer.getAddress(),
                dealer.getMarket(),
                dealer.isActive()
        );
    }

    private FeeDefinitionRecord toRecord(FeeDefinition definition) {
        return new FeeDefinitionRecord(
                definition.getId(),
                definition.getCode(),
                definition.getName(),
                definition.getDescription(),
                definition.isMandatory(),
                definition.getSortOrder(),
                definition.isActive()
        );
    }

    private VehicleRecord toRecord(Vehicle vehicle) {
        return new VehicleRecord(
                vehicle.getId(),
                vehicle.getBrand().getCode(),
                vehicle.getCategory().getCode(),
                vehicle.getModel(),
                vehicle.getName(),
                vehicle.getSeats(),
                vehicle.getVehicleType(),
                vehicle.getYear(),
                vehicle.getEngineCc(),
                vehicle.getFuelType(),
                vehicle.getTransmission(),
                vehicle.getListPrice(),
                vehicle.getDiscountAmount(),
                vehicle.getSalePrice(),
                vehicle.getTaxBasePrice(),
                vehicle.getDefaultDeposit(),
                vehicle.getRegistrationServiceFee(),
                vehicle.getMicaPlateFee(),
                vehicle.getInspectionFee(),
                vehicle.getDefaultColor(),
                vehicle.getAvailableColors(),
                vehicle.getColorPhotos(),
                vehicle.getDeliveryNote(),
                vehicle.getWarrantyNote(),
                vehicle.getGifts(),
                vehicle.getQuoteSheetName(),
                vehicle.getImageUrl(),
                vehicle.getSpecifications(),
                vehicle.isActive()
        );
    }

    private FeeRuleRecord toRecord(FeeRule rule) {
        return new FeeRuleRecord(
                rule.getId(),
                rule.getFeeDefinition().getCode(),
                rule.getCategory() == null ? null : rule.getCategory().getCode(),
                rule.getLocation() == null ? null : rule.getLocation().getCode(),
                rule.getFeeZone() == null ? null : rule.getFeeZone().name(),
                rule.getCalculationType().name(),
                rule.getFixedAmount(),
                rule.getPercentage(),
                rule.getMinAmount(),
                rule.getMaxAmount(),
                rule.getMinEngineCc(),
                rule.getMaxEngineCc(),
                rule.getMinPrice(),
                rule.getMaxPrice(),
                rule.getPriority(),
                rule.getEffectiveFrom(),
                rule.getEffectiveTo(),
                rule.isActive()
        );
    }

    private Brand requireBrand(String code) {
        return brandRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", code));
    }

    private VehicleCategory requireCategory(String code) {
        return categoryRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Category", code));
    }

    private Location requireLocation(String code) {
        return locationRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Location", code));
    }

    private FeeDefinition requireFeeDefinition(String code) {
        return feeDefinitionRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Fee definition", code));
    }

    private static <T> int applyAll(List<T> items, java.util.function.Function<T, ?> writer) {
        if (items == null) {
            return 0;
        }
        items.forEach(writer::apply);
        return items.size();
    }

    private static String codeOrSlug(String code, String name) {
        if (hasText(code)) {
            return code.trim().toUpperCase(Locale.ROOT);
        }
        return PolicyAdminService.slug(name);
    }

    private static void requireText(String value, String label) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(label + " is required");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private static String firstText(String preferred, String fallback) {
        return hasText(preferred) ? preferred.trim() : fallback.trim();
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String raw, String label) {
        if (!hasText(raw)) {
            throw new IllegalArgumentException(label + " is required");
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown " + label + ": " + raw);
        }
    }

    private static <E extends Enum<E>> E optionalEnum(Class<E> type, String raw, String label) {
        if (!hasText(raw)) {
            return null;
        }
        return enumValue(type, raw, label);
    }

}
