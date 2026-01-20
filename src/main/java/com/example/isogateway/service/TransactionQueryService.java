package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionQueryResponse;
import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.exception.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

    private final TransactionRepository repository;

    public TransactionQueryResponse findById(Long id) {
        TransactionEntity entity = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
        return mapToResponse(entity);
    }

    public TransactionQueryResponse findByStan(String stan) {
        TransactionEntity entity = repository.findByStan(stan)
                .orElseThrow(() -> new TransactionNotFoundException("stan", stan));
        return mapToResponse(entity);
    }

    public Page<TransactionQueryResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    private TransactionQueryResponse mapToResponse(TransactionEntity entity) {
        return TransactionQueryResponse.builder()
                .transactionId(entity.getId())
                .stan(entity.getStan())
                .status(entity.getStatus())
                .responseCode(entity.getResponseCode())
                .cardNumberMasked(entity.getCardNumberMasked())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .authorizationCode(entity.getAuthorizationCode())
                .retrievalReferenceNumber(entity.getRetrievalReferenceNumber())
                .processingTimeMs(entity.getProcessingTimeMs())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
