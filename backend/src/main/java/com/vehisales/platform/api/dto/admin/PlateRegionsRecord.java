package com.vehisales.platform.api.dto.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PlateRegionsRecord(
        String defaultArea,
        Map<String, Area> areas,
        Map<String, List<Unit>> regions
) {
    public record Area(BigDecimal amount) {
    }

    public record Unit(String code, String name, String area) {
    }
}
