package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.VehicleDetailResponse;
import com.vehisales.platform.api.dto.VehicleSummaryResponse;
import com.vehisales.platform.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VehicleController {

    private final CatalogService catalogService;

    @GetMapping("/vehicles/search")
    public List<VehicleSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId
    ) {
        return catalogService.searchVehicles(keyword, brand, categoryId);
    }

    @GetMapping("/vehicles")
    public List<VehicleSummaryResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId
    ) {
        return catalogService.searchVehicles(keyword, brand, categoryId);
    }

    @GetMapping("/vehicles/{vehicleId}")
    public VehicleDetailResponse get(@PathVariable Long vehicleId) {
        return catalogService.getVehicle(vehicleId);
    }
}
