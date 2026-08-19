package com.vehisales.platform.api.dto.admin;

import java.math.BigDecimal;

public record FeePolicyRecord(
        BigDecimal registrationTaxPercent,
        BigDecimal registrationTaxCommercialPercent
) {
}
