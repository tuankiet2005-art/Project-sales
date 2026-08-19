package com.vehisales.platform.api.dto;

public record BrandResponse(
        Long id,
        String code,
        String name,
        String tagline,
        String market,
        String accentColor,
        String imageUrl,
        boolean ready
) {
}
