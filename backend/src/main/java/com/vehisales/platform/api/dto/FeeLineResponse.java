package com.vehisales.platform.api.dto;

import java.math.BigDecimal;

public record FeeLineResponse(
        String code,
        String name,
        String description,
        boolean mandatory,
        boolean applicable,
        boolean includedInTotal,
        BigDecimal amount,
        String calculationNote
) {
}
