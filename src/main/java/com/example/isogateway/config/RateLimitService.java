package com.example.isogateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createBucket);
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.getBurstCapacity())
                .refillGreedy(properties.getRequestsPerSecond(), Duration.ofSeconds(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(String key) {
        return resolveBucket(key).tryConsume(1);
    }
}
