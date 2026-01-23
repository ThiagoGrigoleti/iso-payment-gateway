package com.example.isogateway.infrastructure.tcp.client;

import com.example.isogateway.config.BankConnectionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
@RequiredArgsConstructor
public class ConnectionFactory extends BasePooledObjectFactory<PooledConnection> {

    private final BankConnectionProperties connectionProperties;

    @Override
    public PooledConnection create() throws Exception {
        Socket socket = new Socket();
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setSoTimeout(connectionProperties.getReadTimeoutMs());

        socket.connect(
                new InetSocketAddress(connectionProperties.getHost(), connectionProperties.getPort()),
                connectionProperties.getConnectionTimeoutMs()
        );

        log.debug("Created new pooled connection to {}:{}",
                connectionProperties.getHost(), connectionProperties.getPort());

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
