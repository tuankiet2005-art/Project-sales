package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.BrandResponse;
import com.vehisales.platform.api.dto.CategoryResponse;
import com.vehisales.platform.api.dto.LocationResponse;
import com.vehisales.platform.api.dto.VehicleDetailResponse;
import com.vehisales.platform.api.dto.VehicleSummaryResponse;
import com.vehisales.platform.domain.Brand;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DtoMapper {

    public BrandResponse toBrand(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getCode(),
                brand.getName(),
                brand.getTagline(),
                brand.getMarket(),
                brand.getAccentColor(),
                brand.getImageUrl(),
                brand.isReady()
        );
    }

    public CategoryResponse toCategory(VehicleCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription(),
                category.getTypicalSeats(),
                category.isRequiresInspection(),
                category.isRequiresRoadUseFee(),
                category.isRequiresCompulsoryInsurance()
        );
    }

    public LocationResponse toLocation(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getNameEn(),
                location.getNameZh(),
                location.getNameJa(),
                location.getRegion().name(),
                location.getFeeZone().name(),
                location.isCentrallyGovernedCity()
        );
    }

    public VehicleSummaryResponse toSummary(Vehicle vehicle) {
        return new VehicleSummaryResponse(
                vehicle.getId(),
                vehicle.getBrand().getName(),
                vehicle.getBrand().getCode(),
                vehicle.getModel(),
                vehicle.getName(),
                vehicle.getYear(),
                vehicle.getSeats(),
                vehicle.getVehicleType(),
                vehicle.getListPrice(),
                vehicle.getDiscountAmount(),
                salePrice(vehicle),
                vehicle.getImageUrl(),
                toCategory(vehicle.getCategory())
        );
    }

    public VehicleDetailResponse toDetail(Vehicle vehicle) {
        return new VehicleDetailResponse(
                vehicle.getId(),
                vehicle.getBrand().getName(),
                vehicle.getBrand().getCode(),
                vehicle.getModel(),
                vehicle.getName(),
                vehicle.getYear(),
                vehicle.getSeats(),
                vehicle.getVehicleType(),
                vehicle.getEngineCc(),
                vehicle.getFuelType(),
                vehicle.getTransmission(),
                vehicle.getListPrice(),
                vehicle.getDiscountAmount(),
                salePrice(vehicle),
                vehicle.getDefaultDeposit(),
                vehicle.getRegistrationServiceFee(),
                vehicle.getMicaPlateFee(),
                vehicle.getInspectionFee(),
                vehicle.getDefaultColor(),
                vehicle.getAvailableColors(),
                vehicle.getDeliveryNote(),
                vehicle.getWarrantyNote(),
                vehicle.getGifts(),
                vehicle.getImageUrl(),
                vehicle.getSpecifications(),
                toCategory(vehicle.getCategory())
        );
    }

    public BigDecimal salePrice(Vehicle vehicle) {
        if (vehicle.getSalePrice() != null) {
            return vehicle.getSalePrice();
        }
        BigDecimal discount = vehicle.getDiscountAmount() == null ? BigDecimal.ZERO : vehicle.getDiscountAmount();
        return vehicle.getListPrice().subtract(discount);
    }
}
