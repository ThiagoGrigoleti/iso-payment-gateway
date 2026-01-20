package com.example.isogateway.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdGeneratorTest {

    @Test
    void shouldGenerateUniqueStan() {
        Set<String> stans = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            stans.add(TransactionIdGenerator.generateStan());
        }
        assertEquals(1000, stans.size());
    }

    @Test
    void shouldGenerateStanWithSixDigits() {
        String stan = TransactionIdGenerator.generateStan();
        assertEquals(6, stan.length());
        assertTrue(stan.matches("\\d{6}"));
    }

    @Test
    void shouldGenerateRrnWithTwelveDigits() {
        String rrn = TransactionIdGenerator.generateRrn();
        assertEquals(12, rrn.length());
    }

    @Test
    void shouldGenerateAuthCodeWithSixDigits() {
        String authCode = TransactionIdGenerator.generateAuthCode();
        assertEquals(6, authCode.length());
        assertTrue(authCode.matches("\\d{6}"));
    }
}
