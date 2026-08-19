package com.vehisales.platform.api.dto.admin;

public record BrandRecord(
        Long id,
        String code,
        String name,
        String tagline,
        String market,
        String accentColor,
        String imageUrl,
        Boolean ready,
        Integer sortOrder
) {
}
