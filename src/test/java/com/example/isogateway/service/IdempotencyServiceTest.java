package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionResponse;
import com.example.isogateway.config.IdempotencyProperties;
import com.example.isogateway.core.domain.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private IdempotencyProperties properties;
    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        properties = new IdempotencyProperties();
        properties.setTtlSeconds(3600);
        properties.setKeyPrefix("test:");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotencyService = new IdempotencyService(redisTemplate, properties);
    }

    @Test
    void tryAcquire_shouldReturnTrue_whenKeyDoesNotExist() {
        String key = "unique-key-123";
        when(valueOperations.setIfAbsent(eq("test:" + key), eq("PROCESSING"), any(Duration.class)))
                .thenReturn(true);

        boolean result = idempotencyService.tryAcquire(key);

        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq("test:" + key), eq("PROCESSING"), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void tryAcquire_shouldReturnFalse_whenKeyExists() {
        String key = "existing-key";
        when(valueOperations.setIfAbsent(eq("test:" + key), eq("PROCESSING"), any(Duration.class)))
                .thenReturn(false);

        boolean result = idempotencyService.tryAcquire(key);

        assertThat(result).isFalse();
    }

    @Test
    void store_shouldSaveResponseWithTtl() {
        String key = "store-key";
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(1L)
                .stan("123456")
                .status(TransactionStatus.APPROVED)
                .amount(BigDecimal.valueOf(100))
                .build();

        idempotencyService.store(key, response);

        verify(valueOperations).set(eq("test:" + key), eq(response), eq(Duration.ofSeconds(3600)));
    }

    @Test
    void get_shouldReturnResponse_whenExists() {
        String key = "get-key";
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(1L)
                .stan("123456")
                .status(TransactionStatus.APPROVED)
                .build();
        when(valueOperations.get("test:" + key)).thenReturn(response);

        Optional<TransactionResponse> result = idempotencyService.get(key);

        assertThat(result).isPresent();
        assertThat(result.get().getStan()).isEqualTo("123456");
    }

    @Test
    void get_shouldReturnEmpty_whenKeyDoesNotExist() {
        String key = "missing-key";
        when(valueOperations.get("test:" + key)).thenReturn(null);

        Optional<TransactionResponse> result = idempotencyService.get(key);

        assertThat(result).isEmpty();
    }

    @Test
    void get_shouldReturnEmpty_whenProcessingMarker() {
        String key = "processing-key";
        when(valueOperations.get("test:" + key)).thenReturn("PROCESSING");

        Optional<TransactionResponse> result = idempotencyService.get(key);

        assertThat(result).isEmpty();
    }

    @Test
    void isProcessing_shouldReturnTrue_whenMarkerExists() {
        String key = "processing-key";
        when(valueOperations.get("test:" + key)).thenReturn("PROCESSING");

        boolean result = idempotencyService.isProcessing(key);

        assertThat(result).isTrue();
    }

    @Test
    void isProcessing_shouldReturnFalse_whenResponseStored() {
        String key = "completed-key";
        when(valueOperations.get("test:" + key)).thenReturn(TransactionResponse.builder().build());

        boolean result = idempotencyService.isProcessing(key);

        assertThat(result).isFalse();
    }

    @Test
    void remove_shouldDeleteKey() {
        String key = "remove-key";

        idempotencyService.remove(key);

        verify(redisTemplate).delete("test:" + key);
    }
}
