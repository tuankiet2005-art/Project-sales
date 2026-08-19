package com.vehisales.platform.api.dto;

import java.math.BigDecimal;

public record VehicleSummaryResponse(
        Long id,
        String brand,
        String brandCode,
        String model,
        String name,
        Integer year,
        Integer seats,
        String vehicleType,
        BigDecimal listPrice,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        String imageUrl,
        CategoryResponse category
) {
}
