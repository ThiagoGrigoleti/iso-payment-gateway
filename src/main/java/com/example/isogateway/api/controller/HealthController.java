package com.example.isogateway.api.controller;

import com.example.isogateway.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "System", description = "Health check and metrics")
public class HealthController {

    private final MetricsService metricsService;

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "iso-payment-gateway");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Transaction metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}
