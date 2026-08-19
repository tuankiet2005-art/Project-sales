package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.BrandResponse;
import com.vehisales.platform.api.dto.CategoryResponse;
import com.vehisales.platform.api.dto.DealerOfferResponse;
import com.vehisales.platform.api.dto.DealerPolicyResponse;
import com.vehisales.platform.api.dto.LocationResponse;
import com.vehisales.platform.service.CatalogService;
import com.vehisales.platform.service.DealerPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final CatalogService catalogService;
    private final DealerPolicy dealerPolicy;
    private final DataSource dataSource;

    @GetMapping("/brands")
    public List<BrandResponse> brands() {
        return catalogService.listBrands();
    }

    @GetMapping("/brands/{code}")
    public BrandResponse brand(@PathVariable String code) {
        return catalogService.getBrand(code);
    }

    @GetMapping("/vehicle-categories")
    public List<CategoryResponse> categories() {
        return catalogService.listCategories();
    }

    @GetMapping("/locations")
    public List<LocationResponse> locations() {
        return catalogService.listLocations();
    }

    @GetMapping("/dealer-policy")
    public DealerPolicyResponse dealerPolicy() {
        return new DealerPolicyResponse(
                dealerPolicy.discountPercent(com.vehisales.platform.domain.enums.UsageType.PRIVATE),
                dealerPolicy.discountPercent(com.vehisales.platform.domain.enums.UsageType.COMMERCIAL),
                dealerPolicy.offers().stream()
                        .map(offer -> new DealerOfferResponse(
                                offer.getId(),
                                offer.getKind(),
                                offer.getAmount(),
                                offer.getPercent(),
                                offer.getTitle(),
                                offer.getDescription()
                        ))
                        .toList()
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM brands")) {
            resultSet.next();
            body.put("database", "UP");
            body.put("brands", String.valueOf(resultSet.getInt(1)));
        } catch (Exception ex) {
            body.put("status", "DEGRADED");
            body.put("database", "DOWN");
            body.put("databaseError", rootMessage(ex));
        }
        return body;
    }

    private static String rootMessage(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }
}
