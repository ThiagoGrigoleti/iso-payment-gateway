package com.example.isogateway.service;

import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.infrastructure.tcp.client.ConnectionPool;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsService {

    private final TransactionRepository repository;
    private final ConnectionPool connectionPool;
    private final HeartbeatService heartbeatService;
    private final MeterRegistry meterRegistry;

    private Counter approvedCounter;
    private Counter declinedCounter;
    private Counter errorCounter;
    private Counter timeoutCounter;

    public MetricsService(TransactionRepository repository,
                          ConnectionPool connectionPool,
                          HeartbeatService heartbeatService,
                          MeterRegistry meterRegistry) {
        this.repository = repository;
        this.connectionPool = connectionPool;
        this.heartbeatService = heartbeatService;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initMetrics() {
        approvedCounter = Counter.builder("gateway.payment.processed")
                .tag("status", "approved")
                .description("Number of approved payments")
                .register(meterRegistry);

        declinedCounter = Counter.builder("gateway.payment.processed")
                .tag("status", "declined")
                .description("Number of declined payments")
                .register(meterRegistry);

        errorCounter = Counter.builder("gateway.payment.processed")
                .tag("status", "error")
                .description("Number of failed payments")
                .register(meterRegistry);

        timeoutCounter = Counter.builder("gateway.payment.processed")
                .tag("status", "timeout")
                .description("Number of timed out payments")
                .register(meterRegistry);

        Gauge.builder("gateway.connection.pool.active", connectionPool, ConnectionPool::getNumActive)
                .description("Active connections in pool")
                .register(meterRegistry);

        Gauge.builder("gateway.connection.pool.idle", connectionPool, ConnectionPool::getNumIdle)
                .description("Idle connections in pool")
                .register(meterRegistry);

        Gauge.builder("gateway.heartbeat.healthy", heartbeatService, h -> h.isHealthy() ? 1 : 0)
                .description("Bank connection health status")
                .register(meterRegistry);
    }

    public void recordTransaction(TransactionStatus status) {
        switch (status) {
            case APPROVED -> approvedCounter.increment();
            case DECLINED -> declinedCounter.increment();
            case ERROR -> errorCounter.increment();
            case TIMEOUT -> timeoutCounter.increment();
            default -> {}
        }
    }

    public Map<String, Object> getMetrics() {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        LocalDateTime lastDay = LocalDateTime.now().minusDays(1);

        Map<String, Object> metrics = new HashMap<>();

        Map<String, Long> lastHourMetrics = new HashMap<>();
        lastHourMetrics.put("total", repository.count());
        lastHourMetrics.put("approved", repository.countByStatusSince(TransactionStatus.APPROVED, lastHour));
        lastHourMetrics.put("declined", repository.countByStatusSince(TransactionStatus.DECLINED, lastHour));
        lastHourMetrics.put("errors", repository.countByStatusSince(TransactionStatus.ERROR, lastHour));
        lastHourMetrics.put("timeouts", repository.countByStatusSince(TransactionStatus.TIMEOUT, lastHour));

        Map<String, Long> lastDayMetrics = new HashMap<>();
        lastDayMetrics.put("approved", repository.countByStatusSince(TransactionStatus.APPROVED, lastDay));
        lastDayMetrics.put("declined", repository.countByStatusSince(TransactionStatus.DECLINED, lastDay));
        lastDayMetrics.put("errors", repository.countByStatusSince(TransactionStatus.ERROR, lastDay));
        lastDayMetrics.put("timeouts", repository.countByStatusSince(TransactionStatus.TIMEOUT, lastDay));

        Double avgProcessingTime = repository.averageProcessingTimeSince(lastHour);

        Map<String, Object> poolMetrics = new HashMap<>();
        poolMetrics.put("active", connectionPool.getNumActive());
        poolMetrics.put("idle", connectionPool.getNumIdle());
        poolMetrics.put("borrowed", connectionPool.getBorrowedCount());
        poolMetrics.put("returned", connectionPool.getReturnedCount());
        poolMetrics.put("created", connectionPool.getCreatedCount());
        poolMetrics.put("destroyed", connectionPool.getDestroyedCount());

        Map<String, Object> heartbeatMetrics = new HashMap<>();
        heartbeatMetrics.put("healthy", heartbeatService.isHealthy());
        heartbeatMetrics.put("consecutiveFailures", heartbeatService.getConsecutiveFailures());

        metrics.put("lastHour", lastHourMetrics);
        metrics.put("lastDay", lastDayMetrics);
        metrics.put("averageProcessingTimeMs", avgProcessingTime != null ? avgProcessingTime : 0);
        metrics.put("connectionPool", poolMetrics);
        metrics.put("heartbeat", heartbeatMetrics);
        metrics.put("timestamp", LocalDateTime.now());

        return metrics;
    }
}
