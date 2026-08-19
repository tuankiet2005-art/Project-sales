package com.vehisales.platform.service;

import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.enums.FeeCalculationType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeeAmountCalculatorTest {

    private final FeeAmountCalculator calculator = new FeeAmountCalculator();

    @Test
    void calculatesFixedAmount() {
        FeeRule rule = FeeRule.builder()
                .calculationType(FeeCalculationType.FIXED)
                .fixedAmount(new BigDecimal("20000000"))
                .build();
        Vehicle vehicle = Vehicle.builder().listPrice(new BigDecimal("531000000")).build();

        assertThat(calculator.calculate(rule, vehicle)).isEqualByComparingTo("20000000");
    }

    @Test
    void calculatesPercentageOfListPrice() {
        FeeRule rule = FeeRule.builder()
                .calculationType(FeeCalculationType.PERCENT_OF_LIST_PRICE)
                .percentage(new BigDecimal("12"))
                .build();
        Vehicle vehicle = Vehicle.builder().listPrice(new BigDecimal("531000000")).build();

        assertThat(calculator.calculate(rule, vehicle)).isEqualByComparingTo("63720000");
    }

    @Test
    void appliesMinimumBound() {
        FeeRule rule = FeeRule.builder()
                .calculationType(FeeCalculationType.PERCENT_WITH_BOUNDS)
                .percentage(new BigDecimal("1"))
                .minAmount(new BigDecimal("10000000"))
                .build();
        Vehicle vehicle = Vehicle.builder().listPrice(new BigDecimal("100000")).build();

        assertThat(calculator.calculate(rule, vehicle)).isEqualByComparingTo("10000000");
    }
}
