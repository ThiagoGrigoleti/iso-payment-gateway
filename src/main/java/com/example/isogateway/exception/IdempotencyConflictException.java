package com.example.isogateway.exception;

import lombok.Getter;

@Getter
public class IdempotencyConflictException extends RuntimeException {

    private final String idempotencyKey;

    public IdempotencyConflictException(String idempotencyKey) {
        super("Request with idempotency key is already being processed: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }
}
