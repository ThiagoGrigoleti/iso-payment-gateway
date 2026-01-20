package com.example.isogateway.api.dto;

import com.example.isogateway.core.domain.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionQueryResponse {

    private Long transactionId;
    private String stan;
    private TransactionStatus status;
    private String responseCode;
    private String cardNumberMasked;
    private BigDecimal amount;
    private String currency;
    private String authorizationCode;
    private String retrievalReferenceNumber;
    private Long processingTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
