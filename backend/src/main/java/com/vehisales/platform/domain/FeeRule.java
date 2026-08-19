package com.vehisales.platform.domain;

import com.vehisales.platform.domain.enums.FeeCalculationType;
import com.vehisales.platform.domain.enums.FeeZone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_definition_id")
    private FeeDefinition feeDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VehicleCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private FeeZone feeZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FeeCalculationType calculationType;

    @Column(precision = 18, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 8, scale = 4)
    private BigDecimal percentage;

    @Column(precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal maxAmount;

    private Integer minEngineCc;

    private Integer maxEngineCc;

    @Column(precision = 18, scale = 2)
    private BigDecimal minPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false)
    private int priority;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active;
}
