package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import com.example.isogateway.exception.BankConnectionException;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class IsoTcpClient {

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final BankConnectionProperties connectionProperties;
    private final ConnectionPool connectionPool;
    private final Timer tcpTimer;

    public IsoTcpClient(MessageFactory<IsoMessage> isoMessageFactory,
                        BankConnectionProperties connectionProperties,
                        ConnectionPool connectionPool,
                        MeterRegistry meterRegistry) {
        this.isoMessageFactory = isoMessageFactory;
        this.connectionProperties = connectionProperties;
        this.connectionPool = connectionPool;
        this.tcpTimer = Timer.builder("gateway.tcp.request.duration")
                .description("Time spent on TCP communication with bank")
                .register(meterRegistry);
    }

    @CircuitBreaker(name = "bankConnection", fallbackMethod = "sendFallback")
    public IsoMessage send(IsoMessage message, String stan) {
        return tcpTimer.record(() -> doSendWithRetry(message, stan));
    }

    private IsoMessage doSendWithRetry(IsoMessage message, String stan) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < connectionProperties.getMaxRetries()) {
            attempts++;
            PooledConnection connection = null;
            boolean invalidate = false;

            try {
                connection = connectionPool.borrowConnection();
                return doSend(connection, message, stan);
            } catch (SocketTimeoutException e) {
                log.warn("Timeout on attempt {} for STAN {}", attempts, stan);
                lastException = e;
                invalidate = true;
            } catch (IOException e) {
                log.warn("Connection error on attempt {} for STAN {}: {}", attempts, stan, e.getMessage());
                lastException = e;
                invalidate = true;
            } catch (Exception e) {
                log.error("Unexpected error on attempt {} for STAN {}", attempts, stan, e);
                lastException = e;
                invalidate = true;
                break;
            } finally {
                if (connection != null) {
                    if (invalidate) {
                        connectionPool.invalidateConnection(connection);
                    } else {
                        connectionPool.returnConnection(connection);
                    }
                }
            }

            if (attempts < connectionProperties.getMaxRetries()) {
                try {
                    Thread.sleep(connectionProperties.getRetryDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new BankConnectionException(
                "Failed to communicate with bank after " + attempts + " attempts",
                stan,
                lastException
        );
    }

    private IsoMessage doSend(PooledConnection connection, IsoMessage message, String stan)
            throws IOException, java.text.ParseException {

        log.debug("Sending message to bank for STAN {} using pooled connection", stan);

        message.write(connection.getOutputStream(), 2);
        connection.getOutputStream().flush();

        int length = connection.getInputStream().readShort();
        byte[] data = new byte[length];
        connection.getInputStream().readFully(data);

        IsoMessage response = isoMessageFactory.parseMessage(data, 0);

        if (response != null) {
            log.debug("Received response for STAN {}: MTI {}", stan, String.format("%04x", response.getType()));
        }

        connection.touch();
        return response;
    }

    @SuppressWarnings("unused")
    private IsoMessage sendFallback(IsoMessage message, String stan, Exception e) {
        log.error("Circuit breaker open for bank connection, STAN={}", stan);
        throw new BankConnectionException("Bank connection unavailable (circuit breaker open)", stan, e);
    }
}