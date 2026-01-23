package com.example.isogateway.api.controller;

import com.example.isogateway.api.dto.ReconciliationResult;
import com.example.isogateway.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation", description = "Settlement and reconciliation operations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping
    @Operation(summary = "Process reconciliation file")
    public ResponseEntity<ReconciliationResult> reconcile(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("file") MultipartFile bankFile) {
        return ResponseEntity.ok(reconciliationService.reconcile(date, bankFile));
    }
}
