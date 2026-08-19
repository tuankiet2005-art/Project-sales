package com.vehisales.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "dealer-policy")
public class DealerPolicyProperties {

    private Discount discount = new Discount();
    private List<Offer> offers = new ArrayList<>();

    @Getter
    @Setter
    public static class Discount {
        private BigDecimal privatePercent = new BigDecimal("5");
        private BigDecimal commercialPercent = new BigDecimal("8");
    }

    @Getter
    @Setter
    public static class Offer {
        private String id;
        private String kind;
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal percent = BigDecimal.ZERO;
        private Map<String, String> title = new LinkedHashMap<>();
        private Map<String, String> description = new LinkedHashMap<>();
    }
}
