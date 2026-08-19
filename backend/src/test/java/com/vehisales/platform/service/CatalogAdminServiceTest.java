package com.vehisales.platform.service;

import com.vehisales.platform.api.dto.admin.BrandRecord;
import com.vehisales.platform.api.dto.admin.CatalogSnapshot;
import com.vehisales.platform.domain.Brand;
import com.vehisales.platform.repository.BrandRepository;
import com.vehisales.platform.repository.DealerRepository;
import com.vehisales.platform.repository.FeeDefinitionRepository;
import com.vehisales.platform.repository.FeeRuleRepository;
import com.vehisales.platform.repository.LocationRepository;
import com.vehisales.platform.repository.VehicleCategoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogAdminServiceTest {

    @Mock
    private BrandRepository brandRepository;
    @Mock
    private VehicleCategoryRepository categoryRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private DealerRepository dealerRepository;
    @Mock
    private FeeDefinitionRepository feeDefinitionRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private FeeRuleRepository feeRuleRepository;
    @Mock
    private TextTranslateService textTranslateService;

    @InjectMocks
    private CatalogAdminService service;

    @Test
    void importUpsertsBrandByCode() {
        when(brandRepository.findByCodeIgnoreCase("MITSUBISHI")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> {
            Brand brand = invocation.getArgument(0);
            brand.setId(1L);
            return brand;
        });

        var result = service.importAll(new CatalogSnapshot(
                List.of(new BrandRecord(null, "MITSUBISHI", "Mitsubishi", "VN", "Vietnam", "#E60012", null, true, 10)),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        ));

        assertThat(result.brands()).isEqualTo(1);
        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("MITSUBISHI");
        assertThat(captor.getValue().getName()).isEqualTo("Mitsubishi");
    }
}
