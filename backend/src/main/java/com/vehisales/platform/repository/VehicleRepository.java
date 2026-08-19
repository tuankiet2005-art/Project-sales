package com.vehisales.platform.repository;

import com.vehisales.platform.domain.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @EntityGraph(attributePaths = {"category", "brand"})
    List<Vehicle> findByActiveTrueOrderByNameAsc();

    @EntityGraph(attributePaths = {"category", "brand"})
    Optional<Vehicle> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query("""
            SELECT v FROM Vehicle v
            WHERE v.active = true
              AND (:brandCode IS NULL OR LOWER(v.brand.code) = LOWER(:brandCode))
              AND (:categoryId IS NULL OR v.category.id = :categoryId)
              AND (
                    :keyword IS NULL
                 OR :keyword = ''
                 OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(v.brand.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(v.category.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY v.name ASC
            """)
    List<Vehicle> searchActive(
            @Param("keyword") String keyword,
            @Param("brandCode") String brandCode,
            @Param("categoryId") Long categoryId
    );
}
