package com.example.isogateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @Async("notificationExecutor")
    public void sendTransactionNotification(String transactionId, String status, String cardMasked) {
        log.info("Sending notification for transaction={} status={} card={}",
                transactionId, status, cardMasked);
        simulateExternalCall();
        log.debug("Notification sent for transaction={}", transactionId);
    }

    @Async("fraudCheckExecutor")
    public void performFraudAnalysis(String transactionId, String cardNumber, java.math.BigDecimal amount) {
        log.info("Starting fraud analysis for transaction={} amount={}", transactionId, amount);
        simulateExternalCall();
        log.debug("Fraud analysis completed for transaction={}", transactionId);
    }

    private void simulateExternalCall() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
