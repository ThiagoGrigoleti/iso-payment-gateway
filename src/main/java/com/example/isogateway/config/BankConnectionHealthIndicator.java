package com.example.isogateway.config;

import com.example.isogateway.infrastructure.tcp.client.ConnectionPool;
import com.example.isogateway.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankConnectionHealthIndicator implements HealthIndicator {

    private final HeartbeatService heartbeatService;
    private final ConnectionPool connectionPool;

    @Override
    public Health health() {
        if (!heartbeatService.isHealthy()) {
            return Health.down()
                    .withDetail("reason", "Bank connection unhealthy")
                    .withDetail("consecutiveFailures", heartbeatService.getConsecutiveFailures())
                    .build();
        }

        return Health.up()
                .withDetail("poolActive", connectionPool.getNumActive())
                .withDetail("poolIdle", connectionPool.getNumIdle())
                .withDetail("heartbeatHealthy", true)
                .build();
    }
}
