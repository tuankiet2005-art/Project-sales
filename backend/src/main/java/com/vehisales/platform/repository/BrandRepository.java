package com.vehisales.platform.repository;

import com.vehisales.platform.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderBySortOrderAsc();

    Optional<Brand> findByCodeIgnoreCase(String code);
}
