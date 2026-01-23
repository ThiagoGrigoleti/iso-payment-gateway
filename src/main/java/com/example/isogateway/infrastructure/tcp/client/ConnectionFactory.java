package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import com.example.isogateway.config.SslProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class ConnectionFactory extends BasePooledObjectFactory<PooledConnection> {

    private final BankConnectionProperties connectionProperties;
    private final SslProperties sslProperties;
    private final SSLContext sslContext;

    public ConnectionFactory(BankConnectionProperties connectionProperties,
                             SslProperties sslProperties,
                             SSLContext sslContext) {
        this.connectionProperties = connectionProperties;
        this.sslProperties = sslProperties;
        this.sslContext = sslContext;
    }

    @Override
    public PooledConnection create() throws Exception {
        Socket socket;

        if (sslProperties.isEnabled()) {
            SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory()
                    .createSocket(connectionProperties.getHost(), connectionProperties.getPort());
            sslSocket.setSoTimeout(connectionProperties.getReadTimeoutMs());
            sslSocket.setTcpNoDelay(true);
            sslSocket.setKeepAlive(true);
            sslSocket.startHandshake();
            socket = sslSocket;
            log.debug("Created new SSL pooled connection to {}:{}",
                    connectionProperties.getHost(), connectionProperties.getPort());
        } else {
            socket = new Socket();
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(connectionProperties.getReadTimeoutMs());
            socket.connect(
                    new InetSocketAddress(connectionProperties.getHost(), connectionProperties.getPort()),
                    connectionProperties.getConnectionTimeoutMs()
            );
            log.debug("Created new plaintext pooled connection to {}:{}",
                    connectionProperties.getHost(), connectionProperties.getPort());
        }

        return new PooledConnection(socket);
    }

    @Override
    public PooledObject<PooledConnection> wrap(PooledConnection connection) {
        return new DefaultPooledObject<>(connection);
    }

    @Override
    public void destroyObject(PooledObject<PooledConnection> p, DestroyMode mode) throws Exception {
        PooledConnection connection = p.getObject();
        log.debug("Destroying pooled connection, age={}ms", System.currentTimeMillis() - connection.getCreatedAt());
        connection.close();
    }

    @Override
    public boolean validateObject(PooledObject<PooledConnection> p) {
        PooledConnection connection = p.getObject();
        if (!connection.isValid()) {
            return false;
        }
        try {
            int available = connection.getSocket().getInputStream().available();
            return available >= 0 && connection.getSocket().isConnected();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void passivateObject(PooledObject<PooledConnection> p) throws Exception {
        p.getObject().touch();
    }
}
