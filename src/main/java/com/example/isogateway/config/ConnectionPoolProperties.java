package com.example.isogateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "gateway.pool")
public class ConnectionPoolProperties {

    private int maxTotal = 20;
    private int maxIdle = 10;
    private int minIdle = 2;
    private long maxWaitMillis = 5000;
    private boolean testOnBorrow = true;
    private boolean testOnReturn = false;
    private boolean testWhileIdle = true;
    private long timeBetweenEvictionRunsMillis = 30000;
    private long minEvictableIdleTimeMillis = 60000;
}
