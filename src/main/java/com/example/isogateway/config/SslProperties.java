package com.example.isogateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway.ssl")
public class SslProperties {

    private boolean enabled = false;
    private String keystorePath = "gateway-keystore.p12";
    private String keystorePassword = "senha123";
    private String keystoreType = "PKCS12";
    private String truststorePath = "gateway-truststore.jks";
    private String truststorePassword = "senha123";
    private String truststoreType = "JKS";
    private String protocol = "TLSv1.2";
}
