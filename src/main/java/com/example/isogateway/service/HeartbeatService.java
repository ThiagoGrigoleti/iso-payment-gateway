package com.example.isogateway.service;

import com.example.isogateway.config.HeartbeatProperties;
import com.example.isogateway.core.iso.IsoFieldMap;
import com.example.isogateway.infrastructure.tcp.client.ConnectionPool;
import com.example.isogateway.infrastructure.tcp.client.PooledConnection;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private static final int MTI_NETWORK_REQUEST = 0x800;
    private static final int MTI_NETWORK_RESPONSE = 0x810;
    private static final int ECHO_TEST_CODE = 301;

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final ConnectionPool connectionPool;
    private final HeartbeatProperties heartbeatProperties;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    @Scheduled(fixedRateString = "${gateway.heartbeat.interval-ms:30000}")
    public void sendHeartbeat() {
        if (!heartbeatProperties.isEnabled()) {
            return;
        }

        PooledConnection connection = null;
        boolean invalidate = false;

        try {
            connection = connectionPool.borrowConnection();
            IsoMessage echoRequest = buildEchoRequest();

            echoRequest.write(connection.getOutputStream(), 2);
            connection.getOutputStream().flush();

            int length = connection.getInputStream().readShort();
            byte[] data = new byte[length];
            connection.getInputStream().readFully(data);

            IsoMessage response = isoMessageFactory.parseMessage(data, 0);

            if (response != null && response.getType() == MTI_NETWORK_RESPONSE) {
                String responseCode = response.hasField(IsoFieldMap.RESPONSE_CODE)
                        ? response.getObjectValue(IsoFieldMap.RESPONSE_CODE).toString()
                        : "99";

                if ("00".equals(responseCode)) {
                    consecutiveFailures.set(0);
                    log.debug("Heartbeat successful");
                } else {
                    handleFailure("Invalid response code: " + responseCode);
                }
            } else {
                handleFailure("Invalid response MTI");
                invalidate = true;
            }
        } catch (Exception e) {
            handleFailure(e.getMessage());
            invalidate = true;
        } finally {
            if (connection != null) {
                if (invalidate) {
                    connectionPool.invalidateConnection(connection);
                } else {
                    connectionPool.returnConnection(connection);
                }
            }
        }
    }

    private IsoMessage buildEchoRequest() {
        IsoMessage message = new IsoMessage();
        message.setType(MTI_NETWORK_REQUEST);
        message.setValue(7, new Date(), IsoType.DATE10, 10);
        message.setValue(11, generateStan(), IsoType.NUMERIC, 6);
        message.setValue(70, String.format("%03d", ECHO_TEST_CODE), IsoType.NUMERIC, 3);
        return message;
    }

    private String generateStan() {
        return String.format("%06d", (int) (Math.random() * 999999));
    }

    private void handleFailure(String reason) {
        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= heartbeatProperties.getMaxConsecutiveFailures()) {
            log.error("FATAL: Bank connection lost after {} consecutive failures. Reason: {}",
                    failures, reason);
        } else {
            log.warn("Heartbeat failed ({}/{}): {}",
                    failures, heartbeatProperties.getMaxConsecutiveFailures(), reason);
        }
    }

    public boolean isHealthy() {
        return consecutiveFailures.get() < heartbeatProperties.getMaxConsecutiveFailures();
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures.get();
    }
}
