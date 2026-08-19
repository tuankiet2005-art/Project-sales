package com.vehisales.platform.api.dto.admin;

import java.util.List;

public record CatalogSnapshot(
        List<BrandRecord> brands,
        List<CategoryRecord> categories,
        List<LocationRecord> locations,
        List<DealerRecord> dealers,
        List<FeeDefinitionRecord> feeDefinitions,
        List<VehicleRecord> vehicles,
        List<FeeRuleRecord> feeRules
) {
}
