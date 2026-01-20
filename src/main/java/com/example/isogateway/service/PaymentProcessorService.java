package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.api.dto.TransactionResponse;
import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.exception.BankConnectionException;
import com.example.isogateway.exception.DuplicateTransactionException;
import com.example.isogateway.infrastructure.tcp.client.IsoTcpClient;
import com.example.isogateway.util.CardMaskUtil;
import com.example.isogateway.util.TransactionIdGenerator;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final IsoTcpClient isoTcpClient;
    private final TransactionRepository repository;

    @Transactional
    public TransactionResponse process(TransactionRequest request) {
        String stan = TransactionIdGenerator.generateStan();
        String rrn = TransactionIdGenerator.generateRrn();
        String maskedCard = CardMaskUtil.mask(request.getCardNumber());
        long startTime = System.currentTimeMillis();

        log.info("Processing transaction STAN={} Card={} Amount={}",
                stan, CardMaskUtil.maskForLog(request.getCardNumber()), request.getAmount());

        if (repository.existsByStan(stan)) {
            throw new DuplicateTransactionException(stan);
        }

        TransactionEntity entity = TransactionEntity.builder()
                .cardNumberMasked(maskedCard)
                .amount(request.getAmount())
                .currency("BRL")
                .stan(stan)
                .retrievalReferenceNumber(rrn)
                .requestMti("0200")
                .status(TransactionStatus.PENDING)
                .build();
        entity = repository.save(entity);

        try {
            IsoMessage isoRequest = buildIsoMessage(request, stan, rrn);
            entity.setRawRequest(isoRequest.debugString());

            IsoMessage response = isoTcpClient.send(isoRequest, stan);

            long processingTime = System.currentTimeMillis() - startTime;
            entity.setProcessingTimeMs(processingTime);

            if (response != null) {
                entity.setRawResponse(response.debugString());
                entity.setResponseMti(String.format("%04x", response.getType()));

                String responseCode = "99";
                if (response.hasField(IsoFieldMap.RESPONSE_CODE)) {
                    responseCode = response.getObjectValue(IsoFieldMap.RESPONSE_CODE).toString();
                }
                entity.setResponseCode(responseCode);

                if ("00".equals(responseCode)) {
                    entity.setStatus(TransactionStatus.APPROVED);
                    entity.setAuthorizationCode(TransactionIdGenerator.generateAuthCode());
                    repository.save(entity);

                    log.info("Transaction APPROVED STAN={} ResponseCode={} Time={}ms",
                            stan, responseCode, processingTime);

                    return TransactionResponse.success(
                            entity.getId(),
                            stan,
                            responseCode,
                            maskedCard,
                            request.getAmount(),
                            entity.getAuthorizationCode(),
                            processingTime
                    );
                } else {
                    entity.setStatus(TransactionStatus.DECLINED);
                    repository.save(entity);

                    log.info("Transaction DECLINED STAN={} ResponseCode={} Time={}ms",
                            stan, responseCode, processingTime);

                    return TransactionResponse.declined(
                            entity.getId(),
                            stan,
                            responseCode,
                            maskedCard,
                            request.getAmount()
                    );
                }
            } else {
                entity.setStatus(TransactionStatus.ERROR);
                entity.setErrorMessage("Null response from bank");
                repository.save(entity);

                log.error("Null response from bank STAN={}", stan);
                return TransactionResponse.error(stan, "No response from bank");
            }

        } catch (BankConnectionException e) {
            entity.setStatus(TransactionStatus.TIMEOUT);
            entity.setErrorMessage(e.getMessage());
            entity.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            repository.save(entity);

            log.error("Bank connection failed STAN={}: {}", stan, e.getMessage());
            return TransactionResponse.timeout(stan);

        } catch (Exception e) {
            entity.setStatus(TransactionStatus.ERROR);
            entity.setErrorMessage(e.getMessage());
            entity.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            repository.save(entity);

            log.error("Transaction processing error STAN={}", stan, e);
            return TransactionResponse.error(stan, "Processing error: " + e.getMessage());
        }
    }

    private IsoMessage buildIsoMessage(TransactionRequest request, String stan, String rrn) {
        IsoMessage m = isoMessageFactory.newMessage(0x200);
        m.setValue(IsoFieldMap.PAN, request.getCardNumber(), IsoType.LLVAR, 0);
        m.setValue(IsoFieldMap.PROCESSING_CODE, "000000", IsoType.NUMERIC, 6);
        m.setValue(IsoFieldMap.AMOUNT,
                String.format("%012d", request.getAmount().movePointRight(2).longValue()),
                IsoType.NUMERIC, 12);
        m.setValue(IsoFieldMap.TRANSMISSION_DATE, new Date(), IsoType.DATE10, 10);
        m.setValue(IsoFieldMap.STAN, stan, IsoType.NUMERIC, 6);
        return m;
    }
}