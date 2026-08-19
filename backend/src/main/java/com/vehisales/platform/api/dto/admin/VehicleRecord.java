package com.vehisales.platform.api.dto.admin;

import java.math.BigDecimal;
import java.util.Map;

public record VehicleRecord(
        Long id,
        String brandCode,
        String categoryCode,
        String model,
        String name,
        Integer seats,
        String vehicleType,
        Integer year,
        Integer engineCc,
        String fuelType,
        String transmission,
        BigDecimal listPrice,
        BigDecimal discountAmount,
        BigDecimal salePrice,
        BigDecimal taxBasePrice,
        BigDecimal defaultDeposit,
        BigDecimal registrationServiceFee,
        BigDecimal micaPlateFee,
        BigDecimal inspectionFee,
        String defaultColor,
        String availableColors,
        Map<String, String> colorPhotos,
        String deliveryNote,
        String warrantyNote,
        String gifts,
        String quoteSheetName,
        String imageUrl,
        Map<String, String> specifications,
        Boolean active
) {
}
