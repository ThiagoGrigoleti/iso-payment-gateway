package com.example.isogateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway.heartbeat")
public class HeartbeatProperties {

    private boolean enabled = true;
    private long intervalMs = 30000;
    private int maxConsecutiveFailures = 3;
}
