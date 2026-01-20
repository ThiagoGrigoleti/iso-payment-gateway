package com.example.isogateway.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardMaskUtilTest {

    @Test
    void shouldMaskCardNumberCorrectly() {
        String result = CardMaskUtil.mask("4111111111111111");
        assertEquals("411111******1111", result);
    }

    @Test
    void shouldMaskCardWithSpaces() {
        String result = CardMaskUtil.mask("4111 1111 1111 1111");
        assertEquals("411111******1111", result);
    }

    @Test
    void shouldHandleShortCardNumber() {
        String result = CardMaskUtil.mask("12345");
        assertEquals("****", result);
    }

    @Test
    void shouldHandleNullCardNumber() {
        String result = CardMaskUtil.mask(null);
        assertEquals("****", result);
    }

    @Test
    void shouldMaskForLogCorrectly() {
        String result = CardMaskUtil.maskForLog("4111111111111111");
        assertEquals("************1111", result);
    }
}
