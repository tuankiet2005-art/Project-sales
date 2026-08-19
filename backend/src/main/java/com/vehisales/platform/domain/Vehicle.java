package com.vehisales.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false, length = 180)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private VehicleCategory category;

    private Integer seats;

    @Column(length = 40)
    private String vehicleType;

    @Column(name = "model_year")
    private Integer year;

    private Integer engineCc;

    @Column(length = 40)
    private String fuelType;

    @Column(length = 40)
    private String transmission;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal listPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxBasePrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal defaultDeposit;

    @Column(precision = 18, scale = 2)
    private BigDecimal registrationServiceFee;

    @Column(precision = 18, scale = 2)
    private BigDecimal micaPlateFee;

    @Column(precision = 18, scale = 2)
    private BigDecimal inspectionFee;

    @Column(length = 80)
    private String defaultColor;

    @Column(length = 240)
    private String availableColors;

    @Convert(converter = StringMapConverter.class)
    @Column(name = "color_photos", columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> colorPhotos = new LinkedHashMap<>();

    @Column(length = 120)
    private String deliveryNote;

    @Column(length = 400)
    private String warrantyNote;

    @Column(columnDefinition = "TEXT")
    private String gifts;

    @Column(length = 80)
    private String quoteSheetName;

    @Column(length = 1000)
    private String imageUrl;

    @Convert(converter = StringMapConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, String> specifications = new LinkedHashMap<>();

    @Column(nullable = false)
    private boolean active;
}
