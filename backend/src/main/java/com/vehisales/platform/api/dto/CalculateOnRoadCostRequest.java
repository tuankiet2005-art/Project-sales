package com.vehisales.platform.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOnRoadCostRequest(
        @NotNull Long vehicleId,
        @NotNull Long locationId,
        Long categoryId,
        boolean includeOptionalInsurance,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        BigDecimal deposit,
        BigDecimal optionalBodyInsurance,
        BigDecimal registrationServiceFee,
        BigDecimal micaPlateFee,
        BigDecimal inspectionFee,
        List<AccessoryItem> accessories
) {
}
