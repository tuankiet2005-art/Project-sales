package com.vehisales.platform.domain;

import com.vehisales.platform.domain.enums.FeeZone;
import com.vehisales.platform.domain.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 160)
    private String nameEn;

    @Column(nullable = false, length = 160)
    private String nameZh;

    @Column(nullable = false, length = 160)
    private String nameJa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FeeZone feeZone;

    @Column(nullable = false)
    private boolean centrallyGovernedCity;
}
