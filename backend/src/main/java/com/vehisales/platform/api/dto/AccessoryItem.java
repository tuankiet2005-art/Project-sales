package com.vehisales.platform.api.dto;

import java.math.BigDecimal;

public record AccessoryItem(
        String name,
        BigDecimal amount
) {
}
