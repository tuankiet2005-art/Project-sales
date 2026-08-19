package com.vehisales.platform.api.dto;

public record CategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer typicalSeats,
        boolean requiresInspection,
        boolean requiresRoadUseFee,
        boolean requiresCompulsoryInsurance
) {
}
