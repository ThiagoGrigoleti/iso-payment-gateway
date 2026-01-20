package com.example.isogateway.service;

import com.example.isogateway.api.dto.TransactionRequest;
import com.example.isogateway.api.dto.TransactionResponse;
import com.example.isogateway.core.domain.TransactionStatus;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.core.repository.TransactionRepository;
import com.example.isogateway.infrastructure.tcp.client.IsoTcpClient;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorServiceTest {

    @Mock
    private MessageFactory<IsoMessage> isoMessageFactory;

    @Mock
    private IsoTcpClient isoTcpClient;

    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private PaymentProcessorService service;

    private TransactionRequest request;
    private IsoMessage mockIsoMessage;
    private IsoMessage mockResponse;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest();
        request.setCardNumber("4111111111111111");
        request.setAmount(new BigDecimal("100.00"));

        mockIsoMessage = new IsoMessage();
        mockIsoMessage.setType(0x200);

        mockResponse = new IsoMessage();
        mockResponse.setType(0x210);
        mockResponse.setValue(IsoFieldMap.RESPONSE_CODE, "00", IsoType.ALPHA, 2);
    }

    @Test
    void shouldProcessApprovedTransaction() {
        when(repository.existsByStan(anyString())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, com.example.isogateway.core.domain.TransactionEntity.class);
            entity.setId(1L);
            return entity;
        });
        when(isoMessageFactory.newMessage(0x200)).thenReturn(mockIsoMessage);
        when(isoTcpClient.send(any(), anyString())).thenReturn(mockResponse);

        TransactionResponse response = service.process(request);

        assertNotNull(response);
        assertEquals(TransactionStatus.APPROVED, response.getStatus());
        assertEquals("00", response.getResponseCode());
        verify(repository, times(2)).save(any());
    }

    @Test
    void shouldProcessDeclinedTransaction() {
        mockResponse.setValue(IsoFieldMap.RESPONSE_CODE, "51", IsoType.ALPHA, 2);

        when(repository.existsByStan(anyString())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> {
            var entity = inv.getArgument(0, com.example.isogateway.core.domain.TransactionEntity.class);
            entity.setId(1L);
            return entity;
        });
        when(isoMessageFactory.newMessage(0x200)).thenReturn(mockIsoMessage);
        when(isoTcpClient.send(any(), anyString())).thenReturn(mockResponse);

        TransactionResponse response = service.process(request);

        assertNotNull(response);
        assertEquals(TransactionStatus.DECLINED, response.getStatus());
        assertEquals("51", response.getResponseCode());
    }
}
