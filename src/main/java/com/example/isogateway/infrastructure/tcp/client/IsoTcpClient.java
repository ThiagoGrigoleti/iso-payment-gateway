package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import com.example.isogateway.exception.BankConnectionException;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IsoTcpClient {

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final BankConnectionProperties connectionProperties;

    public IsoMessage send(IsoMessage message, String stan) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < connectionProperties.getMaxRetries()) {
            attempts++;
            try {
                return doSend(message, stan);
            } catch (SocketTimeoutException e) {
                log.warn("Timeout on attempt {} for STAN {}", attempts, stan);
                lastException = e;
            } catch (IOException e) {
                log.warn("Connection error on attempt {} for STAN {}: {}", attempts, stan, e.getMessage());
                lastException = e;
            } catch (Exception e) {
                log.error("Unexpected error on attempt {} for STAN {}", attempts, stan, e);
                lastException = e;
                break;
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

    private IsoMessage doSend(IsoMessage message, String stan) throws IOException, java.text.ParseException {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(connectionProperties.getHost(), connectionProperties.getPort()),
                    connectionProperties.getConnectionTimeoutMs()
            );
            socket.setSoTimeout(connectionProperties.getReadTimeoutMs());

            log.debug("Connected to bank {}:{} for STAN {}",
                    connectionProperties.getHost(), connectionProperties.getPort(), stan);

            message.write(socket.getOutputStream(), 2);
            socket.getOutputStream().flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            int length = in.readShort();
            byte[] data = new byte[length];
            in.readFully(data);

            IsoMessage response = isoMessageFactory.parseMessage(data, 0);

            if (response != null) {
                log.debug("Received response for STAN {}: MTI {}", stan, String.format("%04x", response.getType()));
            }

            return response;
        }
    }
}