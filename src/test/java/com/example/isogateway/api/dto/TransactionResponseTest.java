package com.example.isogateway.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        TransactionResponse response = TransactionResponse.success(
                1L, "000001", "00", "411111******1111",
                new BigDecimal("100.00"), "123456", 150L
        );

        assertEquals(1L, response.getTransactionId());
        assertEquals("000001", response.getStan());
        assertEquals("00", response.getResponseCode());
        assertEquals("Approved", response.getResponseDescription());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void shouldCreateDeclinedResponse() {
        TransactionResponse response = TransactionResponse.declined(
                1L, "000001", "51", "411111******1111", new BigDecimal("100.00")
        );

        assertEquals("51", response.getResponseCode());
        assertEquals("Insufficient funds", response.getResponseDescription());
    }

    @Test
    void shouldCreateTimeoutResponse() {
        TransactionResponse response = TransactionResponse.timeout("000001");

        assertEquals("000001", response.getStan());
        assertNotNull(response.getErrorMessage());
    }
}
