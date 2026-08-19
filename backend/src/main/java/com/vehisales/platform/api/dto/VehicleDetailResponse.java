package com.vehisales.platform.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record VehicleDetailResponse(
        Long id,
        String brand,
        String brandCode,
        String model,
        String name,
        Integer year,
        Integer seats,
        String vehicleType,
        Integer engineCc,
        String fuelType,
        String transmission,
        BigDecimal listPrice,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        BigDecimal defaultDeposit,
        BigDecimal registrationServiceFee,
        BigDecimal micaPlateFee,
        BigDecimal inspectionFee,
        String defaultColor,
        String availableColors,
        String deliveryNote,
        String warrantyNote,
        String gifts,
        String imageUrl,
        Map<String, String> specifications,
        CategoryResponse category
) {
}
