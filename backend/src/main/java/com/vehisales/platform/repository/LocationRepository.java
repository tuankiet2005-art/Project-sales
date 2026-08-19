package com.vehisales.platform.repository;

import com.vehisales.platform.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrderByNameAsc();

    Optional<Location> findByCode(String code);

    Optional<Location> findByCodeIgnoreCase(String code);
}
