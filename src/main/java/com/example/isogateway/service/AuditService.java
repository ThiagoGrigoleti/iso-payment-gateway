package com.example.isogateway.service;

import com.example.isogateway.core.domain.AuditLogEntity;
import com.example.isogateway.core.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    public void log(String entityType, Long entityId, String action, String actor, String actorIp,
                    Object oldValue, Object newValue) {
        try {
            AuditLogEntity audit = AuditLogEntity.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(actor)
                .actorIp(actorIp)
                .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                .build();
            auditLogRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    public void logTransaction(Long transactionId, String action, String actor, String actorIp) {
        log("TRANSACTION", transactionId, action, actor, actorIp, null, null);
    }
}
