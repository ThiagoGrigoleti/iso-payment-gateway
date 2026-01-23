package com.example.isogateway.infrastructure.tcp.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class PooledConnection {

    private final Socket socket;
    private final DataInputStream inputStream;
    private final OutputStream outputStream;
    private final long createdAt;
    private long lastUsedAt;

    public PooledConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = socket.getOutputStream();
        this.createdAt = System.currentTimeMillis();
        this.lastUsedAt = this.createdAt;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastUsedAt() {
        return lastUsedAt;
    }

    public void touch() {
        this.lastUsedAt = System.currentTimeMillis();
    }

    public boolean isValid() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
