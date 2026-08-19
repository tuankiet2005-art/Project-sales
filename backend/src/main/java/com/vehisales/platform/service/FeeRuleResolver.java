package com.vehisales.platform.service;

import com.vehisales.platform.domain.FeeDefinition;
import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class FeeRuleResolver {

    public Optional<FeeRule> resolve(
            FeeDefinition definition,
            Vehicle vehicle,
            VehicleCategory selectedCategory,
            Location location,
            List<FeeRule> activeRules
    ) {
        return activeRules.stream()
                .filter(rule -> rule.getFeeDefinition().getId().equals(definition.getId()))
                .filter(rule -> matchesCategory(rule, selectedCategory))
                .filter(rule -> matchesLocation(rule, location))
                .filter(rule -> matchesEngine(rule, vehicle))
                .filter(rule -> matchesPrice(rule, vehicle.getListPrice()))
                .max(Comparator
                        .comparingInt(this::specificityScore)
                        .thenComparingInt(FeeRule::getPriority));
    }

    private boolean matchesCategory(FeeRule rule, VehicleCategory selectedCategory) {
        return rule.getCategory() == null
                || rule.getCategory().getId().equals(selectedCategory.getId());
    }

    private boolean matchesLocation(FeeRule rule, Location location) {
        if (rule.getLocation() != null) {
            return rule.getLocation().getId().equals(location.getId());
        }
        if (rule.getFeeZone() != null) {
            return rule.getFeeZone() == location.getFeeZone();
        }
        return true;
    }

    private boolean matchesEngine(FeeRule rule, Vehicle vehicle) {
        Integer engineCc = vehicle.getEngineCc();
        if (rule.getMinEngineCc() != null) {
            if (engineCc == null || engineCc < rule.getMinEngineCc()) {
                return false;
            }
        }
        if (rule.getMaxEngineCc() != null) {
            if (engineCc == null || engineCc > rule.getMaxEngineCc()) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesPrice(FeeRule rule, BigDecimal listPrice) {
        if (rule.getMinPrice() != null && listPrice.compareTo(rule.getMinPrice()) < 0) {
            return false;
        }
        return rule.getMaxPrice() == null || listPrice.compareTo(rule.getMaxPrice()) <= 0;
    }

    /**
     * More specific rules win: a Hanoi-only car rule beats a national car rule,
     * which beats a national all-category rule.
     */
    private int specificityScore(FeeRule rule) {
        int score = 0;
        if (rule.getCategory() != null) {
            score += 10;
        }
        if (rule.getLocation() != null) {
            score += 20;
        } else if (rule.getFeeZone() != null) {
            score += 10;
        }
        if (rule.getMinEngineCc() != null || rule.getMaxEngineCc() != null) {
            score += 5;
        }
        if (rule.getMinPrice() != null || rule.getMaxPrice() != null) {
            score += 5;
        }
        return score;
    }
}
