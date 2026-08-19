package com.vehisales.platform.repository;

import com.vehisales.platform.domain.FeeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeDefinitionRepository extends JpaRepository<FeeDefinition, Long> {

    List<FeeDefinition> findByActiveTrueOrderBySortOrderAsc();

    Optional<FeeDefinition> findByCode(String code);
}
