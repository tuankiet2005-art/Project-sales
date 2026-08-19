package com.vehisales.platform.service;

import com.vehisales.platform.domain.FeeDefinition;
import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import com.vehisales.platform.domain.enums.FeeZone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeeRuleResolverTest {

    private final FeeRuleResolver resolver = new FeeRuleResolver();

    @Test
    void prefersLocationSpecificRuleOverNationalRule() {
        FeeDefinition definition = FeeDefinition.builder().id(1L).code("REGISTRATION_TAX").build();
        VehicleCategory cars = VehicleCategory.builder().id(3L).code("PASSENGER_CAR_4").build();
        Location hanoi = Location.builder().id(10L).feeZone(FeeZone.SPECIAL).build();

        FeeRule national = FeeRule.builder()
                .id(1L)
                .feeDefinition(definition)
                .category(cars)
                .priority(0)
                .build();
        FeeRule hanoiOnly = FeeRule.builder()
                .id(2L)
                .feeDefinition(definition)
                .category(cars)
                .location(hanoi)
                .priority(0)
                .build();

        Vehicle vehicle = Vehicle.builder()
                .category(cars)
                .listPrice(new BigDecimal("500000000"))
                .build();

        assertThat(resolver.resolve(definition, vehicle, cars, hanoi, List.of(national, hanoiOnly)))
                .contains(hanoiOnly);
    }

    @Test
    void skipsRulesForOtherCategories() {
        FeeDefinition definition = FeeDefinition.builder().id(2L).code("INSPECTION").build();
        VehicleCategory cars = VehicleCategory.builder().id(3L).code("PASSENGER_CAR_4").build();
        VehicleCategory bikes = VehicleCategory.builder().id(1L).code("MOTORCYCLE").build();
        Location hue = Location.builder().id(15L).feeZone(FeeZone.MAJOR).build();

        FeeRule carInspection = FeeRule.builder()
                .id(5L)
                .feeDefinition(definition)
                .category(cars)
                .build();

        Vehicle motorcycle = Vehicle.builder()
                .category(bikes)
                .listPrice(new BigDecimal("52000000"))
                .build();

        assertThat(resolver.resolve(definition, motorcycle, bikes, hue, List.of(carInspection))).isEmpty();
    }
}
