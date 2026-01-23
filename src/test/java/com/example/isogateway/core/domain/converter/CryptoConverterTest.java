package com.example.isogateway.core.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoConverterTest {

    private CryptoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CryptoConverter();
        converter.initializeKey("12345678901234567890123456789012");
    }

    @Test
    void encrypt_shouldReturnEncryptedString() {
        String plainText = "1234567890123456";

        String encrypted = converter.convertToDatabaseColumn(plainText);

        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(encrypted).isNotBlank();
    }

    @Test
    void decrypt_shouldReturnOriginalString() {
        String plainText = "1234567890123456";

        String encrypted = converter.convertToDatabaseColumn(plainText);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    void encrypt_shouldProduceDifferentCiphertexts() {
        String plainText = "1234567890123456";

        String encrypted1 = converter.convertToDatabaseColumn(plainText);
        String encrypted2 = converter.convertToDatabaseColumn(plainText);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNull_whenInputIsNull() {
        String result = converter.convertToDatabaseColumn(null);

        assertThat(result).isNull();
    }

    @Test
    void convertToEntityAttribute_shouldReturnNull_whenInputIsNull() {
        String result = converter.convertToEntityAttribute(null);

        assertThat(result).isNull();
    }

    @Test
    void encryptDecrypt_shouldWorkWithSpecialCharacters() {
        String plainText = "Card: 1234-5678-9012-3456!";

        String encrypted = converter.convertToDatabaseColumn(plainText);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(plainText);
    }
}
