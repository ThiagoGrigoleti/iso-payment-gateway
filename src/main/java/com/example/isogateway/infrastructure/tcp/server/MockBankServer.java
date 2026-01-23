package com.example.isogateway.infrastructure.tcp.server;

import com.example.isogateway.config.SslProperties;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockBankServer implements CommandLineRunner {

    private static final String BANK_KEYSTORE_PATH = "bank-keystore.p12";
    private static final String BANK_KEYSTORE_PASSWORD = "senha123";
    private static final String BANK_TRUSTSTORE_PATH = "bank-truststore.jks";
    private static final String BANK_TRUSTSTORE_PASSWORD = "senha123";

    private final MessageFactory<IsoMessage> isoMessageFactory;
    private final SslProperties sslProperties;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private ExecutorService executor;
    private ServerSocket serverSocket;

    @Override
    public void run(String... args) {
        executor = Executors.newCachedThreadPool();
        executor.execute(this::startServer);
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executor != null) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.warn("Error during shutdown: {}", e.getMessage());
        }
    }

    private void startServer() {
        try {
            serverSocket = createServerSocket();
            log.info("Mock Bank Server started on port 9999, ssl={}", sslProperties.isEnabled());

            while (running.get()) {
                try {
                    Socket client = serverSocket.accept();
                    executor.execute(() -> handle(client));
                } catch (Exception e) {
                    if (running.get()) {
                        log.error("Error accepting connection: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Fatal error in bank server", e);
        }
    }

    private ServerSocket createServerSocket() throws Exception {
        if (sslProperties.isEnabled()) {
            SSLContext sslContext = createBankSslContext();
            SSLServerSocket sslServer = (SSLServerSocket) sslContext
                    .getServerSocketFactory()
                    .createServerSocket(9999);
            sslServer.setNeedClientAuth(true);
            return sslServer;
        }
        return new ServerSocket(9999);
    }

    private SSLContext createBankSslContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(BANK_KEYSTORE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Bank keystore not found: " + BANK_KEYSTORE_PATH);
            }
            keyStore.load(in, BANK_KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, BANK_KEYSTORE_PASSWORD.toCharArray());

        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(BANK_TRUSTSTORE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Bank truststore not found: " + BANK_TRUSTSTORE_PATH);
            }
            trustStore.load(in, BANK_TRUSTSTORE_PASSWORD.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    private void handle(Socket client) {
        try (client) {
            DataInputStream in = new DataInputStream(client.getInputStream());
            int length = in.readShort();
            byte[] data = new byte[length];
            in.readFully(data);

            IsoMessage request = isoMessageFactory.parseMessage(data, 0);

            if (request != null) {
                log.debug("Bank received: MTI={}", String.format("%04x", request.getType()));

                IsoMessage response = createResponse(request);
                response.write(client.getOutputStream(), 2);
                client.getOutputStream().flush();
            }
        } catch (Exception e) {
            log.error("Error processing transaction: {}", e.getMessage());
        }
    }

    private IsoMessage createResponse(IsoMessage request) {
        int requestType = request.getType();

        if (requestType == 0x800) {
            IsoMessage response = isoMessageFactory.createResponse(request);
            response.setType(0x810);
            response.setValue(39, "00", IsoType.ALPHA, 2);
            return response;
        }

        IsoMessage response = isoMessageFactory.createResponse(request);
        response.setType(0x210);
        response.setValue(39, "00", IsoType.ALPHA, 2);
        return response;
    }
}