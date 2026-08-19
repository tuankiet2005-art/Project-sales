package com.vehisales.platform.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOnRoadCostResponse(
        Long vehicleId,
        String vehicleName,
        String brand,
        String model,
        String categoryName,
        Long locationId,
        String locationName,
        BigDecimal listPrice,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        List<FeeLineResponse> fees,
        BigDecimal totalMandatoryFees,
        BigDecimal totalOptionalFees,
        BigDecimal accessoriesTotal,
        BigDecimal estimatedOnRoadTotal,
        BigDecimal deposit,
        List<AccessoryItem> accessories,
        String currency
) {
}
