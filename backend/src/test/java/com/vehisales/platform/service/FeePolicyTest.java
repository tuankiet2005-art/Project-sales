package com.vehisales.platform.service;

import com.vehisales.platform.config.FeePolicyProperties;
import com.vehisales.platform.config.LicensePlateRegionsProperties;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.enums.UsageType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeePolicyTest {

    private final FeePolicy policy = new FeePolicy(taxProperties("10", "2"), plateRegions());

    @Test
    void registrationTaxIsPercentOfCarPrice() {
        assertThat(policy.amount("REGISTRATION_TAX", new BigDecimal("531000000"), UsageType.PRIVATE, hanoi()))
                .isEqualByComparingTo("53100000");
    }

    @Test
    void licensePlateUsesAreaIAmountForHanoiAndHoChiMinh() {
        assertThat(policy.amount("LICENSE_PLATE", new BigDecimal("531000000"), UsageType.PRIVATE, hanoi()))
                .isEqualByComparingTo("20000000");
        assertThat(policy.amount("LICENSE_PLATE", new BigDecimal("531000000"), UsageType.PRIVATE, location("HCM")))
                .isEqualByComparingTo("20000000");
    }

    @Test
    void licensePlateUsesAreaIIAmountForOtherProvinces() {
        assertThat(policy.amount("LICENSE_PLATE", new BigDecimal("531000000"), UsageType.PRIVATE, location("DN")))
                .isEqualByComparingTo("200000");
        assertThat(policy.amount("LICENSE_PLATE", new BigDecimal("531000000"), UsageType.PRIVATE, location("QN")))
                .isEqualByComparingTo("200000");
    }

    @Test
    void unknownLocationFallsBackToDefaultArea() {
        assertThat(policy.amount("LICENSE_PLATE", BigDecimal.ONE, UsageType.PRIVATE, location("XX")))
                .isEqualByComparingTo("200000");
    }

    @Test
    void appliesOnlyToPolicyFees() {
        assertThat(policy.appliesTo("REGISTRATION_TAX")).isTrue();
        assertThat(policy.appliesTo("LICENSE_PLATE")).isTrue();
        assertThat(policy.appliesTo("ROAD_USE")).isFalse();
    }

    @Test
    void commercialRegistrationTaxUsesCommercialPercent() {
        FeePolicy taxPolicy = new FeePolicy(taxProperties("10", "2"), plateRegions());
        assertThat(taxPolicy.amount("REGISTRATION_TAX", new BigDecimal("500000000"), UsageType.PRIVATE, hanoi()))
                .isEqualByComparingTo("50000000");
        assertThat(taxPolicy.amount("REGISTRATION_TAX", new BigDecimal("500000000"), UsageType.COMMERCIAL, hanoi()))
                .isEqualByComparingTo("10000000");
    }

    private static FeePolicyProperties taxProperties(String privatePercent, String commercialPercent) {
        FeePolicyProperties props = new FeePolicyProperties();
        props.setRegistrationTaxPercent(new BigDecimal(privatePercent));
        props.setRegistrationTaxCommercialPercent(new BigDecimal(commercialPercent));
        return props;
    }

    private static LicensePlateRegionsProperties plateRegions() {
        LicensePlateRegionsProperties props = new LicensePlateRegionsProperties();
        LicensePlateRegionsProperties.AreaRate areaI = new LicensePlateRegionsProperties.AreaRate();
        areaI.setAmount(new BigDecimal("20000000"));
        LicensePlateRegionsProperties.AreaRate areaII = new LicensePlateRegionsProperties.AreaRate();
        areaII.setAmount(new BigDecimal("200000"));
        props.setAreas(Map.of("AREA_I", areaI, "AREA_II", areaII));
        props.setRegions(Map.of(
                "NORTH", List.of(unit("HN", "Hà Nội", "AREA_I"), unit("QN", "Quảng Ninh", "AREA_II")),
                "CENTRAL", List.of(unit("DN", "Đà Nẵng", "AREA_II")),
                "SOUTH", List.of(unit("HCM", "Thành phố Hồ Chí Minh", "AREA_I"))
        ));
        return props;
    }

    private static LicensePlateRegionsProperties.Unit unit(String code, String name, String area) {
        LicensePlateRegionsProperties.Unit unit = new LicensePlateRegionsProperties.Unit();
        unit.setCode(code);
        unit.setName(name);
        unit.setArea(area);
        return unit;
    }

    private static Location hanoi() {
        return location("HN");
    }

    private static Location location(String code) {
        return Location.builder().code(code).name(code).build();
    }
}
