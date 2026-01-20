package com.example.isogateway.service;

import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final TransactionRepository repository;

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

        metrics.put("lastHour", lastHourMetrics);
        metrics.put("lastDay", lastDayMetrics);
        metrics.put("averageProcessingTimeMs", avgProcessingTime != null ? avgProcessingTime : 0);
        metrics.put("timestamp", LocalDateTime.now());

        return metrics;
    }
}
