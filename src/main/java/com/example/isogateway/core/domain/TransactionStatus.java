package com.example.isogateway.core.domain;

public enum TransactionStatus {
    PENDING,
    APPROVED,
    DECLINED,
    ERROR,
    TIMEOUT,
    REVERSED,
    REVERSAL_FAILED,
    UNKNOWN
}
