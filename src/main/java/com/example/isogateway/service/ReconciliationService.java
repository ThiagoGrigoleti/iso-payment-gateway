package com.example.isogateway.service;

import com.example.isogateway.api.dto.ReconciliationResult;
import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final TransactionRepository transactionRepository;

    public ReconciliationResult reconcile(LocalDate date, MultipartFile bankFile) {
        List<BankRecord> bankRecords = parseBankFile(bankFile);
        List<TransactionEntity> ourTransactions = getTransactionsForDate(date);

        Map<String, TransactionEntity> ourMap = new HashMap<>();
        for (TransactionEntity tx : ourTransactions) {
            ourMap.put(tx.getStan(), tx);
        }

        List<ReconciliationResult.DiscrepancyItem> discrepancies = new ArrayList<>();
        BigDecimal matchedAmount = BigDecimal.ZERO;
        int matched = 0;

        for (BankRecord bankRecord : bankRecords) {
            TransactionEntity ourTx = ourMap.remove(bankRecord.stan);
            if (ourTx == null) {
                discrepancies.add(ReconciliationResult.DiscrepancyItem.builder()
                    .stan(bankRecord.stan)
                    .type("MISSING_IN_GATEWAY")
                    .bankAmount(bankRecord.amount)
                    .reason("Transaction exists in bank but not in gateway")
                    .build());
            } else if (ourTx.getAmount().compareTo(bankRecord.amount) != 0) {
                discrepancies.add(ReconciliationResult.DiscrepancyItem.builder()
                    .stan(bankRecord.stan)
                    .type("AMOUNT_MISMATCH")
                    .ourAmount(ourTx.getAmount())
                    .bankAmount(bankRecord.amount)
                    .reason("Amount differs between gateway and bank")
                    .build());
            } else {
                matched++;
                matchedAmount = matchedAmount.add(ourTx.getAmount());
            }
        }

        for (TransactionEntity orphan : ourMap.values()) {
            if (orphan.getStatus() == TransactionStatus.APPROVED) {
                discrepancies.add(ReconciliationResult.DiscrepancyItem.builder()
                    .stan(orphan.getStan())
                    .type("MISSING_IN_BANK")
                    .ourAmount(orphan.getAmount())
                    .reason("Transaction exists in gateway but not in bank file")
                    .build());
            }
        }

        BigDecimal totalAmount = ourTransactions.stream()
            .filter(tx -> tx.getStatus() == TransactionStatus.APPROVED)
            .map(TransactionEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bankTotal = bankRecords.stream()
            .map(r -> r.amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReconciliationResult.builder()
            .date(date)
            .totalTransactions(ourTransactions.size())
            .matchedTransactions(matched)
            .unmatchedTransactions(discrepancies.size())
            .totalAmount(totalAmount)
            .matchedAmount(matchedAmount)
            .discrepancy(totalAmount.subtract(bankTotal))
            .discrepancies(discrepancies)
            .build();
    }

    private List<TransactionEntity> getTransactionsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return transactionRepository.findByCreatedAtBetween(start, end);
    }

    private List<BankRecord> parseBankFile(MultipartFile file) {
        List<BankRecord> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    BankRecord record = new BankRecord();
                    record.stan = parts[0].trim();
                    record.amount = new BigDecimal(parts[1].trim());
                    records.add(record);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse bank file: {}", e.getMessage());
        }
        return records;
    }

    private static class BankRecord {
        String stan;
        BigDecimal amount;
    }
}
