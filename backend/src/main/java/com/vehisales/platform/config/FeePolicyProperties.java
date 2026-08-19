package com.vehisales.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "fee-policy")
public class FeePolicyProperties {

    private BigDecimal registrationTaxPercent = new BigDecimal("10");
    private BigDecimal registrationTaxCommercialPercent = new BigDecimal("2");
}
