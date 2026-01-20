package com.example.isogateway.api.controller;

import com.example.isogateway.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Monitoring", description = "Transaction statistics and business metrics")
public class HealthController {

    private final MetricsService metricsService;

    @GetMapping("/stats")
    @Operation(summary = "Transaction statistics", description = "Returns transaction counts and average processing time")
    public ResponseEntity<Map<String, Object>> getTransactionStats() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}
