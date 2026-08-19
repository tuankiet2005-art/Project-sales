package com.vehisales.platform.api.dto.admin;

public record FeeDefinitionRecord(
        Long id,
        String code,
        String name,
        String description,
        Boolean mandatory,
        Integer sortOrder,
        Boolean active
) {
}
