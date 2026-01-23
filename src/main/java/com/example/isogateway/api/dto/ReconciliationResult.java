package com.example.isogateway.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReconciliationResult {

    private LocalDate date;
    private int totalTransactions;
    private int matchedTransactions;
    private int unmatchedTransactions;
    private BigDecimal totalAmount;
    private BigDecimal matchedAmount;
    private BigDecimal discrepancy;
    private List<DiscrepancyItem> discrepancies;

    @Data
    @Builder
    public static class DiscrepancyItem {
        private String stan;
        private String type;
        private BigDecimal ourAmount;
        private BigDecimal bankAmount;
        private String reason;
    }
}
