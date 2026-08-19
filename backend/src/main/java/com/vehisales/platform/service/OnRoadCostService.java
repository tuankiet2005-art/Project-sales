package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.AccessoryItem;
import com.vehisales.platform.api.dto.CalculateOnRoadCostRequest;
import com.vehisales.platform.api.dto.CalculateOnRoadCostResponse;
import com.vehisales.platform.api.dto.FeeLineResponse;
import com.vehisales.platform.domain.FeeDefinition;
import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import com.vehisales.platform.domain.enums.UsageType;
import com.vehisales.platform.exception.ResourceNotFoundException;
import com.vehisales.platform.repository.FeeDefinitionRepository;
import com.vehisales.platform.repository.FeeRuleRepository;
import com.vehisales.platform.repository.LocationRepository;
import com.vehisales.platform.repository.VehicleCategoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnRoadCostService {

    private static final String CURRENCY = "VND";

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final FeeDefinitionRepository feeDefinitionRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final FeeRuleResolver ruleResolver;
    private final FeeAmountCalculator amountCalculator;
    private final FeePolicy feePolicy;
    private final DealerPolicy dealerPolicy;
    private final DtoMapper dtoMapper;

    public CalculateOnRoadCostResponse calculate(CalculateOnRoadCostRequest request) {
        Vehicle vehicle = vehicleRepository.findByIdAndActiveTrue(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", request.vehicleId()));
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", request.locationId()));
        VehicleCategory selectedCategory = request.categoryId() == null
                ? vehicle.getCategory()
                : categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle category", request.categoryId()));

        UsageType usage = UsageType.from(request.usageType());
        DealerPolicy.QuotePricing pricing = dealerPolicy.price(
                vehicle.getListPrice(),
                usage,
                request.selectedOfferIds(),
                request.forgoneOfferIds(),
                request.discountAmount()
        );
        BigDecimal salePrice = request.salePrice() != null ? request.salePrice() : pricing.salePrice();
        BigDecimal discount = request.salePrice() != null
                ? vehicle.getListPrice().subtract(request.salePrice()).max(BigDecimal.ZERO)
                : pricing.discountAmount();

        List<FeeDefinition> definitions = feeDefinitionRepository.findByActiveTrueOrderBySortOrderAsc();
        List<FeeRule> activeRules = feeRuleRepository.findActiveOn(LocalDate.now());

        List<FeeLineResponse> fees = new ArrayList<>();
        BigDecimal totalMandatory = BigDecimal.ZERO;
        BigDecimal totalOptional = BigDecimal.ZERO;

        for (FeeDefinition definition : definitions) {
            var matched = ruleResolver.resolve(definition, vehicle, selectedCategory, location, activeRules);
            var override = overrideAmount(vehicle, definition.getCode(), request);
            if (matched.isEmpty() && override.isEmpty() && !feePolicy.appliesTo(definition.getCode())) {
                continue;
            }

            BigDecimal amount;
            String note;
            if (override.isPresent()) {
                amount = override.get();
                note = "Entered on quote";
            } else if (feePolicy.appliesTo(definition.getCode())) {
                amount = feePolicy.amount(definition.getCode(), salePrice, usage, location);
                note = feePolicy.describe(definition.getCode(), usage, location);
            } else {
                amount = amountCalculator.calculate(matched.get(), vehicle);
                note = amountCalculator.describe(matched.get());
            }
            boolean includeInTotal = definition.isMandatory()
                    || request.includeOptionalInsurance()
                    || ("OPTIONAL_BODY_INSURANCE".equals(definition.getCode())
                    && override.isPresent()
                    && amount.compareTo(BigDecimal.ZERO) > 0);

            if (definition.isMandatory()) {
                totalMandatory = totalMandatory.add(amount);
            } else if (includeInTotal) {
                totalOptional = totalOptional.add(amount);
            }

            fees.add(new FeeLineResponse(
                    definition.getCode(),
                    definition.getName(),
                    definition.getDescription(),
                    definition.isMandatory(),
                    true,
                    includeInTotal,
                    amount,
                    note
            ));
        }

        List<AccessoryItem> accessories = sanitizeAccessories(request.accessories());
        BigDecimal accessoriesTotal = accessories.stream()
                .map(AccessoryItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal estimatedTotal = salePrice.add(totalMandatory).add(totalOptional).add(accessoriesTotal);
        BigDecimal deposit = request.deposit() != null
                ? request.deposit()
                : zeroIfNull(vehicle.getDefaultDeposit());

        return new CalculateOnRoadCostResponse(
                vehicle.getId(),
                vehicle.getName(),
                vehicle.getBrand().getName(),
                vehicle.getModel(),
                selectedCategory.getName(),
                location.getId(),
                location.getName(),
                vehicle.getListPrice(),
                discount,
                salePrice,
                fees,
                totalMandatory,
                totalOptional,
                accessoriesTotal,
                estimatedTotal,
                deposit,
                accessories,
                CURRENCY,
                usage.name(),
                pricing.discountPercent(),
                pricing.appliedOfferIds()
        );
    }

    private java.util.Optional<BigDecimal> overrideAmount(
            Vehicle vehicle,
            String feeCode,
            CalculateOnRoadCostRequest request
    ) {
        return switch (feeCode) {
            case "REGISTRATION_FEE", "REGISTRATION_SERVICE" ->
                    firstPresent(request.registrationServiceFee(), vehicle.getRegistrationServiceFee());
            case "MICA_PLATE" -> firstPresent(request.micaPlateFee(), vehicle.getMicaPlateFee());
            case "INSPECTION" -> firstPresent(request.inspectionFee(), vehicle.getInspectionFee());
            case "OPTIONAL_BODY_INSURANCE" -> java.util.Optional.ofNullable(request.optionalBodyInsurance());
            default -> java.util.Optional.empty();
        };
    }

    private List<AccessoryItem> sanitizeAccessories(List<AccessoryItem> accessories) {
        if (accessories == null) {
            return List.of();
        }
        List<AccessoryItem> cleaned = new ArrayList<>();
        for (AccessoryItem item : accessories) {
            if (item == null || item.name() == null || item.name().isBlank() || item.amount() == null) {
                continue;
            }
            if (item.amount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            cleaned.add(new AccessoryItem(item.name().trim(), item.amount()));
        }
        return cleaned;
    }

    private java.util.Optional<BigDecimal> firstPresent(BigDecimal requestValue, BigDecimal vehicleValue) {
        if (requestValue != null) {
            return java.util.Optional.of(requestValue);
        }
        return java.util.Optional.ofNullable(vehicleValue);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
