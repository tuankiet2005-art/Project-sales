package com.vehisales.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "quote_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_address", length = 400)
    private String customerAddress;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "brand_code", length = 40)
    private String brandCode;

    @Column(name = "vehicle_name", length = 180)
    private String vehicleName;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "location_name", length = 160)
    private String locationName;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 80)
    private String color;

    @Column(name = "usage_type", length = 20)
    private String usageType;

    @Column(length = 8)
    private String language;

    @Column(name = "include_optional", nullable = false)
    private boolean includeOptional;

    @Column(name = "list_price", precision = 18, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "sale_price", precision = 18, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal deposit;

    @Column(name = "on_road_total", precision = 18, scale = 2)
    private BigDecimal onRoadTotal;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
