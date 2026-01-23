package com.example.isogateway.core.domain.converter;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Converter
@Component
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(CryptoConverter.class);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int REQUIRED_KEY_LENGTH = 32;

    private static volatile byte[] encryptionKey;
    private static final Object KEY_LOCK = new Object();

    @Value("${gateway.security.encryption-key:}")
    private String keyFromConfig;

    @PostConstruct
    public void init() {
        synchronized (KEY_LOCK) {
            String envKey = System.getenv("ENCRYPTION_KEY");
            String keyToUse = (envKey != null && !envKey.isBlank()) ? envKey : keyFromConfig;

            if (keyToUse == null || keyToUse.isBlank()) {
                throw new IllegalStateException(
                    "Encryption key not configured. Set ENCRYPTION_KEY environment variable.");
            }

            initializeKey(keyToUse);
        }
    }

    void setKeyFromConfig(String key) {
        this.keyFromConfig = key;
    }

    void initializeKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != REQUIRED_KEY_LENGTH) {
            throw new IllegalStateException(
                String.format("Encryption key must be exactly %d bytes (current: %d bytes)",
                    REQUIRED_KEY_LENGTH, keyBytes.length));
        }

        encryptionKey = Arrays.copyOf(keyBytes, keyBytes.length);
        Arrays.fill(keyBytes, (byte) 0);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes());
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            return new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
