package com.example.isogateway.exception;

public class TransactionException extends RuntimeException {

    private final String errorCode;
    private final String stan;

    public TransactionException(String message, String errorCode, String stan) {
        super(message);
        this.errorCode = errorCode;
        this.stan = stan;
    }

    public TransactionException(String message, String errorCode, String stan, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.stan = stan;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getStan() {
        return stan;
    }
}
