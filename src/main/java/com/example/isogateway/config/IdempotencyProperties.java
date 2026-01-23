package com.example.isogateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway.idempotency")
public class IdempotencyProperties {

    private long ttlSeconds = 86400;
    private String keyPrefix = "idempotency:";
}
