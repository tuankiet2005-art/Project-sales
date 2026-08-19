package com.vehisales.platform.repository;

import com.vehisales.platform.domain.QuoteHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteHistoryRepository extends JpaRepository<QuoteHistory, Long> {

    List<QuoteHistory> findTop100ByOrderByCreatedAtDesc();

    List<QuoteHistory> findTop100ByCustomerNameContainingIgnoreCaseOrVehicleNameContainingIgnoreCaseOrderByCreatedAtDesc(
            String customerName,
            String vehicleName
    );

    Optional<QuoteHistory> findFirstByCustomerNameIgnoreCaseAndVehicleIdOrderByCreatedAtDesc(
            String customerName,
            Long vehicleId
    );
}
