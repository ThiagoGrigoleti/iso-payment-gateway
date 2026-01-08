package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.infrastructure.tcp.client.IsoTcpClient;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentProcessorService {

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final IsoTcpClient isoTcpClient;

    public String process(TransactionRequest request) {
        try {
            IsoMessage m = isoMessageFactory.newMessage(0x200);
            m.setValue(IsoFieldMap.PAN, request.getCardNumber(), IsoType.LLVAR, 0);
            m.setValue(IsoFieldMap.AMOUNT, String.format("%012d", request.getAmount().movePointRight(2).longValue()), IsoType.NUMERIC, 12);
            m.setValue(IsoFieldMap.TRANSMISSION_DATE, new Date(), IsoType.DATE10, 10);
            m.setValue(IsoFieldMap.STAN, String.format("%06d", new Random().nextInt(999999)), IsoType.NUMERIC, 6);

            IsoMessage response = isoTcpClient.send(m);
            
            return response != null ? "Bank Response: " + response.debugString() : "No Response";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}