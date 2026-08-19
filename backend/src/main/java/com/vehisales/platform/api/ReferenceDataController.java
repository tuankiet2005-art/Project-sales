package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.BrandResponse;
import com.vehisales.platform.api.dto.CategoryResponse;
import com.vehisales.platform.api.dto.LocationResponse;
import com.vehisales.platform.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final CatalogService catalogService;

    @GetMapping("/brands")
    public List<BrandResponse> brands() {
        return catalogService.listBrands();
    }

    @GetMapping("/brands/{code}")
    public BrandResponse brand(@PathVariable String code) {
        return catalogService.getBrand(code);
    }

    @GetMapping("/vehicle-categories")
    public List<CategoryResponse> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/locations")
    public List<LocationResponse> locations() {
        return catalogService.listLocations();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
