package com.vehisales.platform.repository;

import com.vehisales.platform.domain.FeeRule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FeeRuleRepository extends JpaRepository<FeeRule, Long> {

    @EntityGraph(attributePaths = {"feeDefinition", "category", "location"})
    @Query("""
            SELECT r FROM FeeRule r
            WHERE r.active = true
              AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= :onDate)
              AND (r.effectiveTo IS NULL OR r.effectiveTo >= :onDate)
            """)
    List<FeeRule> findActiveOn(@Param("onDate") LocalDate onDate);
}
