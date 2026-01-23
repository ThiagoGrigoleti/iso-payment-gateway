package com.example.isogateway.security;

import java.io.Closeable;
import java.util.Arrays;

public final class SecureCardNumber implements Closeable, AutoCloseable {

    private final char[] value;
    private volatile boolean wiped = false;

    public SecureCardNumber(char[] cardNumber) {
        if (cardNumber == null) {
            this.value = null;
        } else {
            this.value = Arrays.copyOf(cardNumber, cardNumber.length);
        }
    }

    public SecureCardNumber(String cardNumber) {
        if (cardNumber == null) {
            this.value = null;
        } else {
            this.value = cardNumber.toCharArray();
        }
    }

    public char[] getValue() {
        if (wiped) {
            throw new IllegalStateException("SecureCardNumber has been wiped");
        }
        return value;
    }

    public String getValueAsString() {
        if (wiped) {
            throw new IllegalStateException("SecureCardNumber has been wiped");
        }
        return value != null ? new String(value) : null;
    }

    public String getMasked() {
        if (value == null || value.length < 4) {
            return "****";
        }
        String lastFour = new String(value, value.length - 4, 4);
        return "****-****-****-" + lastFour;
    }

    public void wipe() {
        if (value != null && !wiped) {
            Arrays.fill(value, '\0');
            wiped = true;
        }
    }

    public boolean isWiped() {
        return wiped;
    }

    @Override
    public void close() {
        wipe();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            wipe();
        } finally {
            super.finalize();
        }
    }

    @Override
    public String toString() {
        return "SecureCardNumber[" + (wiped ? "WIPED" : "ACTIVE") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
