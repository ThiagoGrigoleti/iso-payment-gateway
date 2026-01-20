package com.example.isogateway.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class TransactionIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicInteger COUNTER = new AtomicInteger(RANDOM.nextInt(900000));
    private static final DateTimeFormatter RRN_FORMAT = DateTimeFormatter.ofPattern("yyDDDHHmmss");

    private TransactionIdGenerator() {
    }

    public static String generateStan() {
        int next = COUNTER.updateAndGet(val -> (val + 1) % 1000000);
        return String.format("%06d", next);
    }

    public static String generateRrn() {
        return LocalDateTime.now().format(RRN_FORMAT) + String.format("%01d", RANDOM.nextInt(10));
    }

    public static String generateAuthCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }
}
