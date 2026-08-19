package com.vehisales.platform.api.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DealerOfferResponse(
        String id,
        String kind,
        BigDecimal amount,
        BigDecimal percent,
        Map<String, String> title,
        Map<String, String> description
) {
}
