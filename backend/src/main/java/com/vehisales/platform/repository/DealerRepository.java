package com.vehisales.platform.repository;

import com.vehisales.platform.domain.Dealer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    @EntityGraph(attributePaths = "brand")
    List<Dealer> findAllByOrderByNameAsc();

    Optional<Dealer> findByBrand_CodeIgnoreCaseAndNameIgnoreCase(String brandCode, String name);

    boolean existsByBrand_Id(Long brandId);
}
