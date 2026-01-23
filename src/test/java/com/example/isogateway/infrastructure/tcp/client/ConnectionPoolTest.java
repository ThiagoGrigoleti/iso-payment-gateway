package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import com.example.isogateway.config.ConnectionPoolProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionPoolTest {

    private ConnectionPool connectionPool;
    private ServerSocket serverSocket;
    private ExecutorService serverExecutor;
    private AtomicInteger connectionCount;

    @BeforeEach
    void setUp() throws IOException {
        connectionCount = new AtomicInteger(0);
        serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();

        serverExecutor = Executors.newCachedThreadPool();
        serverExecutor.submit(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    connectionCount.incrementAndGet();
                    serverExecutor.submit(() -> {
                        try {
                            while (!client.isClosed()) {
                                Thread.sleep(100);
                            }
                        } catch (Exception ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            }
        });

        BankConnectionProperties bankProperties = new BankConnectionProperties();
        bankProperties.setHost("localhost");
        bankProperties.setPort(port);
        bankProperties.setConnectionTimeoutMs(1000);
        bankProperties.setReadTimeoutMs(5000);

        ConnectionPoolProperties poolProperties = new ConnectionPoolProperties();
        poolProperties.setMaxTotal(3);
        poolProperties.setMaxIdle(2);
        poolProperties.setMinIdle(0);
        poolProperties.setMaxWaitMillis(1000);
        poolProperties.setTestOnBorrow(false);

        connectionPool = new ConnectionPool(bankProperties, poolProperties);
        connectionPool.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (connectionPool != null) {
            connectionPool.shutdown();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
    }

    @Test
    void borrowConnection_shouldCreateNewConnection() throws Exception {
        PooledConnection connection = connectionPool.borrowConnection();

        assertThat(connection).isNotNull();
        assertThat(connection.isValid()).isTrue();
        assertThat(connectionPool.getNumActive()).isEqualTo(1);

        connectionPool.returnConnection(connection);
        assertThat(connectionPool.getNumIdle()).isEqualTo(1);
    }

    @Test
    void borrowConnection_shouldReuseIdleConnection() throws Exception {
        PooledConnection first = connectionPool.borrowConnection();
        connectionPool.returnConnection(first);

        PooledConnection second = connectionPool.borrowConnection();

        assertThat(connectionPool.getCreatedCount()).isEqualTo(1);
        assertThat(connectionCount.get()).isEqualTo(1);

        connectionPool.returnConnection(second);
    }

    @Test
    void borrowConnection_shouldRespectMaxTotal() throws Exception {
        PooledConnection c1 = connectionPool.borrowConnection();
        PooledConnection c2 = connectionPool.borrowConnection();
        PooledConnection c3 = connectionPool.borrowConnection();

        assertThat(connectionPool.getNumActive()).isEqualTo(3);

        connectionPool.returnConnection(c1);
        connectionPool.returnConnection(c2);
        connectionPool.returnConnection(c3);
    }

    @Test
    void invalidateConnection_shouldRemoveFromPool() throws Exception {
        PooledConnection connection = connectionPool.borrowConnection();
        connectionPool.invalidateConnection(connection);

        assertThat(connectionPool.getNumActive()).isEqualTo(0);
        assertThat(connectionPool.getDestroyedCount()).isEqualTo(1);
    }

    @Test
    void metrics_shouldTrackPoolStatistics() throws Exception {
        PooledConnection c1 = connectionPool.borrowConnection();
        PooledConnection c2 = connectionPool.borrowConnection();

        assertThat(connectionPool.getBorrowedCount()).isEqualTo(2);

        connectionPool.returnConnection(c1);
        connectionPool.returnConnection(c2);

        assertThat(connectionPool.getReturnedCount()).isEqualTo(2);
        assertThat(connectionPool.getNumIdle()).isEqualTo(2);
    }
}
