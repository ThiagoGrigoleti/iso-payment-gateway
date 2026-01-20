package com.example.isogateway.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(Long id) {
        super("Transaction not found with id: " + id);
    }

    public TransactionNotFoundException(String field, String value) {
        super("Transaction not found with " + field + ": " + value);
    }
}
