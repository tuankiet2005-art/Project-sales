package com.vehisales.platform.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DealerPolicyResponse(
        BigDecimal privateDiscountPercent,
        BigDecimal commercialDiscountPercent,
        List<DealerOfferResponse> offers
) {
}
