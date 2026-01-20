package com.example.isogateway.api.controller;

import com.example.isogateway.api.dto.TransactionQueryResponse;
import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.api.dto.TransactionResponse;
import com.example.isogateway.service.PaymentProcessorService;
import com.example.isogateway.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and query operations")
public class PaymentController {

    private final PaymentProcessorService processorService;
    private final TransactionQueryService queryService;

    @PostMapping
    @Operation(summary = "Process a new payment transaction")
    public ResponseEntity<TransactionResponse> createPayment(
            @RequestBody @Valid TransactionRequest request) {
        TransactionResponse response = processorService.process(request);
        HttpStatus status = switch (response.getStatus()) {
            case APPROVED -> HttpStatus.OK;
            case DECLINED -> HttpStatus.OK;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case ERROR -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionQueryResponse> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @GetMapping("/stan/{stan}")
    @Operation(summary = "Get transaction by STAN")
    public ResponseEntity<TransactionQueryResponse> getTransactionByStan(@PathVariable String stan) {
        return ResponseEntity.ok(queryService.findByStan(stan));
    }

    @GetMapping
    @Operation(summary = "List all transactions with pagination")
    public ResponseEntity<Page<TransactionQueryResponse>> listTransactions(Pageable pageable) {
        return ResponseEntity.ok(queryService.findAll(pageable));
    }
}