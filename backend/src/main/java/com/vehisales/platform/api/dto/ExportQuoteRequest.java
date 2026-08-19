package com.vehisales.platform.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ExportQuoteRequest(
        @NotNull Long vehicleId,
        @NotNull Long locationId,
        Long categoryId,
        boolean includeOptionalInsurance,
        @NotBlank String customerName,
        String customerAddress,
        String color,
        String language,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        BigDecimal deposit,
        BigDecimal optionalBodyInsurance,
        BigDecimal registrationServiceFee,
        BigDecimal micaPlateFee,
        BigDecimal inspectionFee,
        List<AccessoryItem> accessories,
        String usageType,
        List<String> selectedOfferIds,
        List<String> forgoneOfferIds
) {
    public CalculateOnRoadCostRequest toCalculateRequest() {
        return new CalculateOnRoadCostRequest(
                vehicleId,
                locationId,
                categoryId,
                includeOptionalInsurance,
                discountAmount,
                salePrice,
                deposit,
                optionalBodyInsurance,
                registrationServiceFee,
                micaPlateFee,
                inspectionFee,
                accessories,
                usageType,
                selectedOfferIds,
                forgoneOfferIds
        );
    }
}
