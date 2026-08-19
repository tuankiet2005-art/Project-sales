package com.vehisales.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehisales.platform.api.dto.CalculateOnRoadCostResponse;
import com.vehisales.platform.api.dto.ExportQuoteRequest;
import com.vehisales.platform.domain.Brand;
import com.vehisales.platform.domain.QuoteHistory;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.repository.QuoteHistoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteHistoryServiceTest {

    @Mock
    private QuoteHistoryRepository quotes;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private OnRoadCostService onRoadCostService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private QuoteHistoryService service;

    @Test
    void persistStoresCustomerAndTotal() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand(Brand.builder().code("MITSUBISHI").name("Mitsubishi").build());
        when(quotes.findFirstByCustomerNameIgnoreCaseAndVehicleIdOrderByCreatedAtDesc("Nguyen Van A", 9L))
                .thenReturn(Optional.empty());
        when(quotes.save(any(QuoteHistory.class))).thenAnswer(invocation -> {
            QuoteHistory row = invocation.getArgument(0);
            row.setId(15L);
            return row;
        });

        var calc = new CalculateOnRoadCostResponse(
                9L, "Xpander Premium", "Mitsubishi", "Xpander", "Ô tô 7 chỗ",
                1L, "Hà Nội", new BigDecimal("659000000"), new BigDecimal("30000000"),
                new BigDecimal("629000000"), List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("700000000"), BigDecimal.ZERO, List.of(),
                "VND", "PRIVATE", new BigDecimal("5"), List.of()
        );
        var request = new ExportQuoteRequest(
                9L, 1L, 4L, false, "Nguyen Van A", "Thu Duc", "Trắng", "vi",
                null, null, null, null, null, null, null, List.of(), "PRIVATE", List.of(), List.of()
        );

        var saved = service.persist(request, calc, vehicle);

        ArgumentCaptor<QuoteHistory> captor = ArgumentCaptor.forClass(QuoteHistory.class);
        verify(quotes).save(captor.capture());
        assertThat(captor.getValue().getCustomerName()).isEqualTo("Nguyen Van A");
        assertThat(captor.getValue().getBrandCode()).isEqualTo("MITSUBISHI");
        assertThat(saved.onRoadTotal()).isEqualByComparingTo("700000000");
    }
}
