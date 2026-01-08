package com.example.isogateway.api.controller;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.service.PaymentProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessorService service;

    @PostMapping
    public String createPayment(@RequestBody TransactionRequest request) {
        return service.process(request);
    }
}