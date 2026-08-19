package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.BrandResponse;
import com.vehisales.platform.api.dto.CategoryResponse;
import com.vehisales.platform.api.dto.LocationResponse;
import com.vehisales.platform.api.dto.VehicleDetailResponse;
import com.vehisales.platform.api.dto.VehicleSummaryResponse;
import com.vehisales.platform.exception.ResourceNotFoundException;
import com.vehisales.platform.repository.BrandRepository;
import com.vehisales.platform.repository.LocationRepository;
import com.vehisales.platform.repository.VehicleCategoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final BrandRepository brandRepository;
    private final DtoMapper mapper;

    public List<BrandResponse> listBrands() {
        return brandRepository.findAllByOrderBySortOrderAsc().stream()
                .map(mapper::toBrand)
                .toList();
    }

    public BrandResponse getBrand(String code) {
        return brandRepository.findByCodeIgnoreCase(code)
                .map(mapper::toBrand)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", code));
    }

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(mapper::toCategory)
                .toList();
    }

    public List<LocationResponse> listLocations() {
        return locationRepository.findAllByOrderByNameAsc().stream()
                .map(mapper::toLocation)
                .toList();
    }

    public List<VehicleSummaryResponse> searchVehicles(String keyword, String brandCode, Long categoryId) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        String normalizedBrand = brandCode == null || brandCode.isBlank() ? null : brandCode.trim();
        return vehicleRepository.searchActive(normalizedKeyword, normalizedBrand, categoryId).stream()
                .map(mapper::toSummary)
                .toList();
    }

    public VehicleDetailResponse getVehicle(Long id) {
        return vehicleRepository.findByIdAndActiveTrue(id)
                .map(mapper::toDetail)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", id));
    }
}
