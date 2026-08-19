package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.ExportQuoteRequest;
import com.vehisales.platform.api.dto.QuoteHistoryRecord;
import com.vehisales.platform.service.QuoteHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteHistoryController {

    private final QuoteHistoryService quoteHistoryService;

    @GetMapping
    public List<QuoteHistoryRecord> list(@RequestParam(required = false) String q) {
        return quoteHistoryService.list(q);
    }

    @GetMapping("/{id}")
    public QuoteHistoryRecord get(@PathVariable Long id) {
        return quoteHistoryService.get(id);
    }

    @PostMapping
    public QuoteHistoryRecord save(@Valid @RequestBody ExportQuoteRequest request) {
        return quoteHistoryService.saveFromRequest(request);
    }
}
