package com.example.isogateway.exception;

public class DuplicateTransactionException extends RuntimeException {

    private final String stan;

    public DuplicateTransactionException(String stan) {
        super("Transaction with STAN " + stan + " already exists");
        this.stan = stan;
    }

    public String getStan() {
        return stan;
    }
}
