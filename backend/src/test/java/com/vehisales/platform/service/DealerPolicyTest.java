package com.vehisales.platform.service;

import com.vehisales.platform.config.DealerPolicyProperties;
import com.vehisales.platform.domain.enums.UsageType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DealerPolicyTest {

    @Test
    void commercialDiscountIsHigherThanPrivate() {
        DealerPolicy policy = policy();
        assertThat(policy.discountPercent(UsageType.PRIVATE)).isEqualByComparingTo("5");
        assertThat(policy.discountPercent(UsageType.COMMERCIAL)).isEqualByComparingTo("8");
    }

    @Test
    void appliesConfiguredPercentToListPrice() {
        DealerPolicy.QuotePricing priced = policy().price(
                new BigDecimal("500000000"),
                UsageType.PRIVATE,
                List.of(),
                List.of(),
                null
        );
        assertThat(priced.discountAmount()).isEqualByComparingTo("25000000");
        assertThat(priced.salePrice()).isEqualByComparingTo("475000000");
    }

    @Test
    void forgoingGiftAccessoriesCreditsTheVehiclePrice() {
        DealerPolicy.QuotePricing priced = policy().price(
                new BigDecimal("500000000"),
                UsageType.PRIVATE,
                List.of(),
                List.of("gift-accessories"),
                null
        );
        assertThat(priced.discountAmount()).isEqualByComparingTo("40000000");
        assertThat(priced.salePrice()).isEqualByComparingTo("460000000");
        assertThat(priced.appliedOfferIds()).contains("gift-accessories");
    }

    @Test
    void extraPercentStacksOnUsageDiscount() {
        DealerPolicy.QuotePricing priced = policy().price(
                new BigDecimal("500000000"),
                UsageType.PRIVATE,
                List.of("month-campaign"),
                List.of(),
                null
        );
        assertThat(priced.discountAmount()).isEqualByComparingTo("32500000");
    }

    private static DealerPolicy policy() {
        DealerPolicyProperties props = new DealerPolicyProperties();
        props.getDiscount().setPrivatePercent(new BigDecimal("5"));
        props.getDiscount().setCommercialPercent(new BigDecimal("8"));
        DealerPolicyProperties.Offer gift = new DealerPolicyProperties.Offer();
        gift.setId("gift-accessories");
        gift.setKind(DealerPolicy.FORGO_FOR_CREDIT);
        gift.setAmount(new BigDecimal("15000000"));
        DealerPolicyProperties.Offer campaign = new DealerPolicyProperties.Offer();
        campaign.setId("month-campaign");
        campaign.setKind(DealerPolicy.EXTRA_PERCENT);
        campaign.setPercent(new BigDecimal("1.5"));
        props.setOffers(List.of(gift, campaign));
        return new DealerPolicy(props);
    }
}
