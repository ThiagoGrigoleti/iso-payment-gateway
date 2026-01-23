package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionResponse;
import com.example.isogateway.config.IdempotencyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String PROCESSING_MARKER = "PROCESSING";

    private final RedisTemplate<String, Object> redisTemplate;
    private final IdempotencyProperties properties;

    public boolean tryAcquire(String idempotencyKey) {
        String key = buildKey(idempotencyKey);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, PROCESSING_MARKER, Duration.ofSeconds(properties.getTtlSeconds()));
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired idempotency lock for key={}", idempotencyKey);
            return true;
        }
        log.debug("Idempotency key already exists key={}", idempotencyKey);
        return false;
    }

    public void store(String idempotencyKey, TransactionResponse response) {
        String key = buildKey(idempotencyKey);
        redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(properties.getTtlSeconds()));
        log.debug("Stored response for idempotency key={}", idempotencyKey);
    }

    public Optional<TransactionResponse> get(String idempotencyKey) {
        String key = buildKey(idempotencyKey);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null || PROCESSING_MARKER.equals(value)) {
            return Optional.empty();
        }
        return Optional.of((TransactionResponse) value);
    }

    public boolean isProcessing(String idempotencyKey) {
        String key = buildKey(idempotencyKey);
        Object value = redisTemplate.opsForValue().get(key);
        return PROCESSING_MARKER.equals(value);
    }

    public void remove(String idempotencyKey) {
        String key = buildKey(idempotencyKey);
        redisTemplate.delete(key);
        log.debug("Removed idempotency key={}", idempotencyKey);
    }

    private String buildKey(String idempotencyKey) {
        return properties.getKeyPrefix() + idempotencyKey;
    }
}
