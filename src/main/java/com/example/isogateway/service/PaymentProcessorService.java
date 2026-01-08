package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionRequest;
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

    public String process(TransactionRequest request) {
        try {
            IsoMessage m = isoMessageFactory.newMessage(0x200);
            m.setValue(2, request.getCardNumber(), IsoType.LLVAR, 0);
            m.setValue(4, String.format("%012d", request.getAmount().movePointRight(2).longValue()), IsoType.NUMERIC, 12);
            m.setValue(7, new Date(), IsoType.DATE10, 10);
            m.setValue(11, String.format("%06d", new Random().nextInt(999999)), IsoType.NUMERIC, 6);

            return m.debugString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}