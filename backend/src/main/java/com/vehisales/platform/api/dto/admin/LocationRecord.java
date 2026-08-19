package com.vehisales.platform.api.dto.admin;

public record LocationRecord(
        Long id,
        String code,
        String name,
        String nameEn,
        String nameZh,
        String nameJa,
        String region,
        String feeZone,
        Boolean centrallyGovernedCity
) {
}
