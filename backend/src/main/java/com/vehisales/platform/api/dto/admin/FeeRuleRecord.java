package com.vehisales.platform.api.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRuleRecord(
        Long id,
        String feeDefinitionCode,
        String categoryCode,
        String locationCode,
        String feeZone,
        String calculationType,
        BigDecimal fixedAmount,
        BigDecimal percentage,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Integer minEngineCc,
        Integer maxEngineCc,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer priority,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Boolean active
) {
}
