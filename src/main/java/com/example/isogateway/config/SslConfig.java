package com.example.isogateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SslConfig {

    private final SslProperties sslProperties;

    @Bean
    public SSLContext gatewaySslContext() throws Exception {
        if (!sslProperties.isEnabled()) {
            log.info("SSL disabled, returning default SSLContext");
            return SSLContext.getDefault();
        }

        KeyStore keyStore = KeyStore.getInstance(sslProperties.getKeystoreType());
        char[] keystorePassword = sslProperties.getKeystorePassword().toCharArray();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(sslProperties.getKeystorePath())) {
            if (in == null) {
                throw new IllegalStateException("Keystore not found: " + sslProperties.getKeystorePath());
            }
            keyStore.load(in, keystorePassword);
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keystorePassword);

        KeyStore trustStore = KeyStore.getInstance(sslProperties.getTruststoreType());
        char[] truststorePassword = sslProperties.getTruststorePassword().toCharArray();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(sslProperties.getTruststorePath())) {
            if (in == null) {
                throw new IllegalStateException("Truststore not found: " + sslProperties.getTruststorePath());
            }
            trustStore.load(in, truststorePassword);
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance(sslProperties.getProtocol());
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        log.info("SSL context initialized with keystore: {}, truststore: {}",
                sslProperties.getKeystorePath(), sslProperties.getTruststorePath());
        return sslContext;
    }
}
