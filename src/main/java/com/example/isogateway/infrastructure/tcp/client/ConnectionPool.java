package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import com.example.isogateway.config.ConnectionPoolProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionPool {

    private final BankConnectionProperties bankProperties;
    private final ConnectionPoolProperties poolProperties;

    private GenericObjectPool<PooledConnection> pool;

    @PostConstruct
    public void init() {
        GenericObjectPoolConfig<PooledConnection> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(poolProperties.getMaxTotal());
        config.setMaxIdle(poolProperties.getMaxIdle());
        config.setMinIdle(poolProperties.getMinIdle());
        config.setMaxWait(java.time.Duration.ofMillis(poolProperties.getMaxWaitMillis()));
        config.setTestOnBorrow(poolProperties.isTestOnBorrow());
        config.setTestOnReturn(poolProperties.isTestOnReturn());
        config.setTestWhileIdle(poolProperties.isTestWhileIdle());
        config.setTimeBetweenEvictionRuns(java.time.Duration.ofMillis(poolProperties.getTimeBetweenEvictionRunsMillis()));
        config.setMinEvictableIdleTime(java.time.Duration.ofMillis(poolProperties.getMinEvictableIdleTimeMillis()));
        config.setJmxEnabled(true);
        config.setJmxNamePrefix("BankConnectionPool");

        ConnectionFactory factory = new ConnectionFactory(bankProperties);
        pool = new GenericObjectPool<>(factory, config);

        log.info("Connection pool initialized: maxTotal={}, minIdle={}, maxIdle={}",
                poolProperties.getMaxTotal(), poolProperties.getMinIdle(), poolProperties.getMaxIdle());
    }

    @PreDestroy
    public void shutdown() {
        if (pool != null) {
            log.info("Shutting down connection pool");
            pool.close();
        }
    }

    public PooledConnection borrowConnection() throws Exception {
        return pool.borrowObject();
    }

    public void returnConnection(PooledConnection connection) {
        if (connection != null) {
            pool.returnObject(connection);
        }
    }

    public void invalidateConnection(PooledConnection connection) {
        if (connection != null) {
            try {
                pool.invalidateObject(connection);
            } catch (Exception e) {
                log.warn("Failed to invalidate connection: {}", e.getMessage());
            }
        }
    }

    public int getNumActive() {
        return pool.getNumActive();
    }

    public int getNumIdle() {
        return pool.getNumIdle();
    }

    public long getBorrowedCount() {
        return pool.getBorrowedCount();
    }

    public long getReturnedCount() {
        return pool.getReturnedCount();
    }

    public long getCreatedCount() {
        return pool.getCreatedCount();
    }

    public long getDestroyedCount() {
        return pool.getDestroyedCount();
    }
}
