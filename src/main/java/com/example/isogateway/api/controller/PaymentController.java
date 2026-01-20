package com.example.isogateway.api.controller;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.service.PaymentProcessorService;
import jakarta.validation.Valid; 
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessorService service;

    @PostMapping
    public String createPayment(@RequestBody @Valid TransactionRequest request) { 
        return service.process(request);
    }
}