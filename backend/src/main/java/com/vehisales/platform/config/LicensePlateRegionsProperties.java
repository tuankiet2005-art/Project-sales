package com.vehisales.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "license-plate-regions")
public class LicensePlateRegionsProperties {

    private String defaultArea = "AREA_II";
    private Map<String, AreaRate> areas = new LinkedHashMap<>();
    private Map<String, List<Unit>> regions = new LinkedHashMap<>();

    public BigDecimal amountFor(String locationCode) {
        AreaRate rate = areas.get(areaFor(locationCode));
        if (rate == null || rate.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return rate.getAmount();
    }

    public String areaFor(String locationCode) {
        Unit unit = unitFor(locationCode);
        if (unit != null && unit.getArea() != null && !unit.getArea().isBlank()) {
            return unit.getArea();
        }
        return defaultArea == null ? "AREA_II" : defaultArea;
    }

    public Unit unitFor(String locationCode) {
        if (locationCode == null || locationCode.isBlank()) {
            return null;
        }
        String needle = locationCode.trim().toUpperCase(Locale.ROOT);
        for (List<Unit> units : regions.values()) {
            if (units == null) {
                continue;
            }
            for (Unit unit : units) {
                if (unit.getCode() != null && needle.equals(unit.getCode().trim().toUpperCase(Locale.ROOT))) {
                    return unit;
                }
            }
        }
        return null;
    }

    public List<Unit> allUnits() {
        List<Unit> all = new ArrayList<>();
        for (List<Unit> units : regions.values()) {
            if (units != null) {
                all.addAll(units);
            }
        }
        return all;
    }

    @Getter
    @Setter
    public static class AreaRate {
        private BigDecimal amount = BigDecimal.ZERO;
    }

    @Getter
    @Setter
    public static class Unit {
        private String code;
        private String name;
        private String area;
    }
}
