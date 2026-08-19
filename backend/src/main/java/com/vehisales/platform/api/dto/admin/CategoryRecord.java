package com.vehisales.platform.api.dto.admin;

public record CategoryRecord(
        Long id,
        String code,
        String name,
        String description,
        Integer typicalSeats,
        Boolean requiresInspection,
        Boolean requiresRoadUseFee,
        Boolean requiresCompulsoryInsurance,
        Integer sortOrder
) {
}
