package com.example.isogateway.util;

public final class CardMaskUtil {

    private CardMaskUtil() {
    }

    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("\\s", "");
        int length = cleaned.length();
        String firstSix = cleaned.substring(0, 6);
        String lastFour = cleaned.substring(length - 4);
        String masked = "*".repeat(length - 10);
        return firstSix + masked + lastFour;
    }

    public static String maskForLog(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) {
            return "****";
        }
        String cleaned = cardNumber.replaceAll("\\s", "");
        int length = cleaned.length();
        String lastFour = cleaned.substring(length - 4);
        return "*".repeat(length - 4) + lastFour;
    }
}
