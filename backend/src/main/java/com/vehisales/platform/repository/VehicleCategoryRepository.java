package com.vehisales.platform.repository;

import com.vehisales.platform.domain.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    List<VehicleCategory> findAllByOrderBySortOrderAsc();

    Optional<VehicleCategory> findByCode(String code);
}
