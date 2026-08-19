package com.vehisales.platform.api.dto.admin;

import com.vehisales.platform.api.dto.DealerOfferResponse;

import java.math.BigDecimal;
import java.util.List;

public record DealerPolicyRecord(
        BigDecimal privateDiscountPercent,
        BigDecimal commercialDiscountPercent,
        List<DealerOfferResponse> offers
) {
}
