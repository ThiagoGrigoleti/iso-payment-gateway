package com.example.isogateway.api.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private String cardNumber;
    private BigDecimal amount;
}