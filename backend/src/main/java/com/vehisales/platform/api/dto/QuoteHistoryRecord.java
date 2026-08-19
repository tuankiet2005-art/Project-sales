package com.vehisales.platform.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteHistoryRecord(
        Long id,
        String customerName,
        String customerAddress,
        Long vehicleId,
        String brandCode,
        String vehicleName,
        Long locationId,
        String locationName,
        Long categoryId,
        String color,
        String usageType,
        String language,
        boolean includeOptional,
        BigDecimal listPrice,
        BigDecimal salePrice,
        BigDecimal discountAmount,
        BigDecimal deposit,
        BigDecimal onRoadTotal,
        String payload,
        Instant createdAt
) {
}
