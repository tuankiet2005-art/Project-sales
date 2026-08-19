package com.vehisales.platform.service;

import com.vehisales.platform.config.DealerPolicyProperties;
import com.vehisales.platform.domain.enums.UsageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DealerPolicy {

    public static final String FORGO_FOR_CREDIT = "FORGO_FOR_CREDIT";
    public static final String EXTRA_PERCENT = "EXTRA_PERCENT";
    public static final String PRICE_CREDIT = "PRICE_CREDIT";

    private final DealerPolicyProperties properties;

    public BigDecimal discountPercent(UsageType usage) {
        DealerPolicyProperties.Discount discount = properties.getDiscount();
        if (discount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = usage.isCommercial() ? discount.getCommercialPercent() : discount.getPrivatePercent();
        return value == null ? BigDecimal.ZERO : value;
    }

    public QuotePricing price(
            BigDecimal listPrice,
            UsageType usage,
            List<String> selectedOfferIds,
            List<String> forgoneOfferIds,
            BigDecimal overrideDiscount
    ) {
        BigDecimal list = listPrice == null ? BigDecimal.ZERO : listPrice;
        BigDecimal percent = discountPercent(usage);
        BigDecimal baseDiscount = overrideDiscount != null
                ? overrideDiscount
                : percentOf(list, percent);
        Set<String> selected = selectedOfferIds == null ? Set.of() : Set.copyOf(selectedOfferIds);
        Set<String> forgone = forgoneOfferIds == null ? Set.of() : Set.copyOf(forgoneOfferIds);

        BigDecimal extraPercent = BigDecimal.ZERO;
        BigDecimal policyCredit = BigDecimal.ZERO;
        List<String> applied = new ArrayList<>();
        for (DealerPolicyProperties.Offer offer : properties.getOffers()) {
            if (offer.getId() == null) {
                continue;
            }
            String kind = offer.getKind() == null ? "" : offer.getKind();
            if (FORGO_FOR_CREDIT.equals(kind) && forgone.contains(offer.getId())) {
                policyCredit = policyCredit.add(zeroIfNull(offer.getAmount()));
                applied.add(offer.getId());
            } else if (PRICE_CREDIT.equals(kind) && selected.contains(offer.getId())) {
                policyCredit = policyCredit.add(zeroIfNull(offer.getAmount()));
                applied.add(offer.getId());
            } else if (EXTRA_PERCENT.equals(kind) && selected.contains(offer.getId())) {
                extraPercent = extraPercent.add(zeroIfNull(offer.getPercent()));
                applied.add(offer.getId());
            }
        }
        BigDecimal extraDiscount = percentOf(list, extraPercent);
        BigDecimal totalDiscount = baseDiscount.add(extraDiscount).add(policyCredit);
        if (totalDiscount.compareTo(list) > 0) {
            totalDiscount = list;
        }
        return new QuotePricing(list, percent, totalDiscount, list.subtract(totalDiscount), applied);
    }

    public List<DealerPolicyProperties.Offer> offers() {
        return properties.getOffers();
    }

    private BigDecimal percentOf(BigDecimal price, BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(percentage).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record QuotePricing(
            BigDecimal listPrice,
            BigDecimal discountPercent,
            BigDecimal discountAmount,
            BigDecimal salePrice,
            List<String> appliedOfferIds
    ) {
    }
}
