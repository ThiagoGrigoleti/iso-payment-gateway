package com.example.isogateway.core.repository;

import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.domain.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByStan(String stan);

    List<TransactionEntity> findByCardNumberMasked(String cardNumberMasked);

    Page<TransactionEntity> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t WHERE t.createdAt BETWEEN :start AND :end")
    List<TransactionEntity> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.status = :status AND t.createdAt >= :since")
    long countByStatusSince(@Param("status") TransactionStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.processingTimeMs) FROM TransactionEntity t WHERE t.createdAt >= :since")
    Double averageProcessingTimeSince(@Param("since") LocalDateTime since);

    boolean existsByStan(String stan);
}
