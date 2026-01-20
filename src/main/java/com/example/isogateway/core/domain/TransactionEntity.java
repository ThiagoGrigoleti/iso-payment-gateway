package com.example.isogateway.core.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_stan", columnList = "stan"),
    @Index(name = "idx_card_number", columnList = "cardNumberMasked"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 19)
    private String cardNumberMasked;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, unique = true, length = 6)
    private String stan;

    @Column(nullable = false, length = 4)
    private String requestMti;

    @Column(length = 4)
    private String responseMti;

    @Column(length = 2)
    private String responseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 12)
    private String retrievalReferenceNumber;

    @Column(length = 6)
    private String authorizationCode;

    @Column
    private Long processingTimeMs;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(length = 2048)
    private String rawRequest;

    @Column(length = 2048)
    private String rawResponse;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
        if (currency == null) {
            currency = "BRL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
