package com.example.isogateway.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.Arrays;

@Getter
@Setter
public class TransactionRequest implements Closeable, AutoCloseable {

    @NotNull(message = "Card number is required")
    private char[] cardNumber;

    private volatile boolean cardWiped = false;

    @NotNull
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    public void setCardNumber(String cardNumber) {
        if (cardNumber != null) {
            if (!cardNumber.matches("\\d{16}")) {
                throw new IllegalArgumentException("Card number must have 16 numeric digits");
            }
            this.cardNumber = cardNumber.toCharArray();
            this.cardWiped = false;
        } else {
            this.cardNumber = null;
        }
    }

    public void setCardNumber(char[] cardNumber) {
        this.cardNumber = cardNumber;
        this.cardWiped = false;
    }

    @JsonIgnore
    public String getCardNumberAsString() {
        if (cardWiped || cardNumber == null) {
            return null;
        }
        return new String(cardNumber);
    }

    @JsonIgnore
    public String getCardNumberMasked() {
        if (cardNumber == null || cardNumber.length < 4) {
            return "****";
        }
        return "****-****-****-" + new String(cardNumber, cardNumber.length - 4, 4);
    }

    public void wipeCardNumber() {
        if (cardNumber != null && !cardWiped) {
            Arrays.fill(cardNumber, '\0');
            cardWiped = true;
        }
    }

    @JsonIgnore
    public boolean isCardWiped() {
        return cardWiped;
    }

    @Override
    public void close() {
        wipeCardNumber();
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "cardNumber=" + getCardNumberMasked() +
                ", amount=" + amount +
                '}';
    }
}