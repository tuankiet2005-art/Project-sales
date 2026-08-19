package com.vehisales.platform.api.dto.admin;

public record ImportResult(
        int brands,
        int categories,
        int locations,
        int dealers,
        int feeDefinitions,
        int vehicles,
        int feeRules
) {
}
