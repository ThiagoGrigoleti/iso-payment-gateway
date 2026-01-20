package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.core.domain.TransactionEntity;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.infrastructure.tcp.client.IsoTcpClient;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final IsoTcpClient isoTcpClient;
    private final TransactionRepository repository; 

    public String process(TransactionRequest request) {
        try {
         
            String stan = String.format("%06d", new Random().nextInt(999999));
            
            TransactionEntity entity = TransactionEntity.builder()
                    .cardNumber(request.getCardNumber())
                    .amount(request.getAmount())
                    .stan(stan)
                    .requestMti("0200")
                    .build();
            entity = repository.save(entity);

          
            IsoMessage m = isoMessageFactory.newMessage(0x200);
            m.setValue(IsoFieldMap.PAN, request.getCardNumber(), IsoType.LLVAR, 0);
            m.setValue(IsoFieldMap.AMOUNT, String.format("%012d", request.getAmount().movePointRight(2).longValue()), IsoType.NUMERIC, 12);
            m.setValue(IsoFieldMap.TRANSMISSION_DATE, new Date(), IsoType.DATE10, 10);
            m.setValue(IsoFieldMap.STAN, stan, IsoType.NUMERIC, 6);

            
            IsoMessage response = isoTcpClient.send(m);

            if (response != null) {
                entity.setResponseMti(String.format("%04x", response.getType()));
                if (response.hasField(IsoFieldMap.RESPONSE_CODE)) {
                    entity.setResponseCode(response.getObjectValue(IsoFieldMap.RESPONSE_CODE).toString());
                }
                entity.setUpdatedAt(LocalDateTime.now());
                repository.save(entity);
                return "Aprovado: " + entity.getResponseCode();
            } else {
                return "Erro: Sem resposta do banco";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}