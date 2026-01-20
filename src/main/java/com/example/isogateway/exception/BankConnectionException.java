package com.example.isogateway.exception;

public class BankConnectionException extends TransactionException {

    public BankConnectionException(String message, String stan) {
        super(message, "BANK_CONNECTION_ERROR", stan);
    }

    public BankConnectionException(String message, String stan, Throwable cause) {
        super(message, "BANK_CONNECTION_ERROR", stan, cause);
    }
}
