package com.vehisales.platform.service;

import com.vehisales.platform.config.FeePolicyProperties;
import com.vehisales.platform.config.LicensePlateRegionsProperties;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.enums.UsageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class FeePolicy {

    static final String REGISTRATION_TAX = "REGISTRATION_TAX";
    static final String LICENSE_PLATE = "LICENSE_PLATE";

    private final FeePolicyProperties properties;
    private final LicensePlateRegionsProperties plateRegions;

    public static boolean isPolicyOwned(String feeCode) {
        return REGISTRATION_TAX.equals(feeCode) || LICENSE_PLATE.equals(feeCode);
    }

    public boolean appliesTo(String feeCode) {
        return isPolicyOwned(feeCode);
    }

    public BigDecimal amount(String feeCode, BigDecimal carPrice, UsageType usage, Location location) {
        if (LICENSE_PLATE.equals(feeCode)) {
            return plateRegions.amountFor(location == null ? null : location.getCode());
        }
        BigDecimal base = carPrice == null ? BigDecimal.ZERO : carPrice;
        return base.multiply(taxPercent(usage)).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    public String describe(String feeCode, UsageType usage, Location location) {
        if (LICENSE_PLATE.equals(feeCode)) {
            String area = plateRegions.areaFor(location == null ? null : location.getCode());
            LicensePlateRegionsProperties.Unit unit = plateRegions.unitFor(
                    location == null ? null : location.getCode());
            String place = unit != null && unit.getName() != null ? unit.getName() : "default";
            return "Fixed plate fee — " + area + " (" + place + ")";
        }
        return taxPercent(usage).stripTrailingZeros().toPlainString() + "% of selling price";
    }

    private BigDecimal taxPercent(UsageType usage) {
        if (usage != null && usage.isCommercial()) {
            return zeroIfNull(properties.getRegistrationTaxCommercialPercent());
        }
        return zeroIfNull(properties.getRegistrationTaxPercent());
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
