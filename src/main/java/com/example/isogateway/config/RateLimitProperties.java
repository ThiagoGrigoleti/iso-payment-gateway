package com.example.isogateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.ratelimit")
public class RateLimitProperties {
    private int requestsPerSecond = 100;
    private int burstCapacity = 150;
}
