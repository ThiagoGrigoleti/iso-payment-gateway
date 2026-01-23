package com.example.isogateway.service;

import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.infrastructure.tcp.client.IsoTcpClient;
import com.example.isogateway.util.TransactionIdGenerator;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReversalService {

    private static final int MTI_REVERSAL = 0x400;

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final IsoTcpClient isoTcpClient;
    private final TransactionRepository transactionRepository;

    @Async
    @Transactional
    public void triggerReversal(TransactionEntity original, String reason) {
        log.warn("Triggering reversal for STAN={} reason={}", original.getStan(), reason);

        String reversalStan = TransactionIdGenerator.generateStan();
        original.setReversalStan(reversalStan);
        transactionRepository.save(original);

        try {
            IsoMessage reversalRequest = buildReversalMessage(original, reversalStan);
            IsoMessage response = isoTcpClient.send(reversalRequest, reversalStan);

            if (response != null && response.hasField(IsoFieldMap.RESPONSE_CODE)) {
                String responseCode = response.getObjectValue(IsoFieldMap.RESPONSE_CODE).toString();
                if ("00".equals(responseCode) || "400".equals(responseCode)) {
                    original.setStatus(TransactionStatus.REVERSED);
                    log.info("Reversal successful STAN={} reversalStan={}", original.getStan(), reversalStan);
                } else {
                    original.setStatus(TransactionStatus.REVERSAL_FAILED);
                    log.error("Reversal declined STAN={} responseCode={}", original.getStan(), responseCode);
                }
            } else {
                original.setStatus(TransactionStatus.REVERSAL_FAILED);
                log.error("No response for reversal STAN={}", original.getStan());
            }
        } catch (Exception e) {
            original.setStatus(TransactionStatus.REVERSAL_FAILED);
            original.setErrorMessage("Reversal failed: " + e.getMessage());
            log.error("Reversal exception STAN={}: {}", original.getStan(), e.getMessage());
        }

        transactionRepository.save(original);
    }

    private IsoMessage buildReversalMessage(TransactionEntity original, String reversalStan) {
        IsoMessage m = isoMessageFactory.newMessage(MTI_REVERSAL);
        m.setValue(IsoFieldMap.PROCESSING_CODE, "000000", IsoType.NUMERIC, 6);
        m.setValue(IsoFieldMap.AMOUNT,
            String.format("%012d", original.getAmount().movePointRight(2).longValue()),
            IsoType.NUMERIC, 12);
        m.setValue(IsoFieldMap.TRANSMISSION_DATE, new Date(), IsoType.DATE10, 10);
        m.setValue(IsoFieldMap.STAN, reversalStan, IsoType.NUMERIC, 6);
        m.setValue(90, original.getStan(), IsoType.NUMERIC, 6);
        return m;
    }
}
