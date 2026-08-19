package com.vehisales.platform.service;

import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Vehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FeeAmountCalculator {

    public BigDecimal calculate(FeeRule rule, Vehicle vehicle) {
        BigDecimal amount = switch (rule.getCalculationType()) {
            case FIXED -> nullToZero(rule.getFixedAmount());
            case PERCENT_OF_LIST_PRICE -> percentOf(taxBase(vehicle), rule.getPercentage());
            case PERCENT_WITH_BOUNDS -> applyBounds(
                    percentOf(taxBase(vehicle), rule.getPercentage()),
                    rule.getMinAmount(),
                    rule.getMaxAmount()
            );
        };
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal taxBase(Vehicle vehicle) {
        if (vehicle.getTaxBasePrice() != null) {
            return vehicle.getTaxBasePrice();
        }
        return vehicle.getListPrice();
    }

    public String describe(FeeRule rule) {
        return switch (rule.getCalculationType()) {
            case FIXED -> "Fixed amount";
            case PERCENT_OF_LIST_PRICE -> formatPercent(rule.getPercentage()) + " of list price";
            case PERCENT_WITH_BOUNDS -> {
                String bounds = "";
                if (rule.getMinAmount() != null) {
                    bounds += ", min " + rule.getMinAmount().toPlainString();
                }
                if (rule.getMaxAmount() != null) {
                    bounds += ", max " + rule.getMaxAmount().toPlainString();
                }
                yield formatPercent(rule.getPercentage()) + " of list price" + bounds;
            }
        };
    }

    private BigDecimal percentOf(BigDecimal price, BigDecimal percentage) {
        if (percentage == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyBounds(BigDecimal amount, BigDecimal min, BigDecimal max) {
        BigDecimal result = amount;
        if (min != null && result.compareTo(min) < 0) {
            result = min;
        }
        if (max != null && result.compareTo(max) > 0) {
            result = max;
        }
        return result;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatPercent(BigDecimal percentage) {
        if (percentage == null) {
            return "0%";
        }
        return percentage.stripTrailingZeros().toPlainString() + "%";
    }
}
