package com.vehisales.platform.api.dto;

public record LocationResponse(
        Long id,
        String code,
        String name,
        String nameEn,
        String nameZh,
        String nameJa,
        String region,
        String feeZone,
        boolean centrallyGovernedCity
) {
}
