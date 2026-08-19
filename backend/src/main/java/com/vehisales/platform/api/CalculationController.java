package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.CalculateOnRoadCostRequest;
import com.vehisales.platform.api.dto.CalculateOnRoadCostResponse;
import com.vehisales.platform.api.dto.ExportQuoteRequest;
import com.vehisales.platform.service.OnRoadCostService;
import com.vehisales.platform.service.QuoteExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CalculationController {

    private final OnRoadCostService onRoadCostService;
    private final QuoteExportService quoteExportService;

    @PostMapping("/calculate-on-road-cost")
    public CalculateOnRoadCostResponse calculate(@Valid @RequestBody CalculateOnRoadCostRequest request) {
        return onRoadCostService.calculate(request);
    }

    @PostMapping("/export-quote")
    public ResponseEntity<byte[]> exportQuote(@Valid @RequestBody ExportQuoteRequest request) {
        byte[] file = quoteExportService.export(request);
        String filename = quoteExportService.filename(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
