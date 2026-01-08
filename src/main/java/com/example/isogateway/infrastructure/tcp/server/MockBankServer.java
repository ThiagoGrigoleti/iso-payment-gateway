package com.example.isogateway.infrastructure.tcp.server;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class MockBankServer implements CommandLineRunner {

    private final MessageFactory<IsoMessage> isoMessageFactory;

    @Override
    public void run(String... args) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try (ServerSocket server = new ServerSocket(9999)) {
                System.out.println("Mock Bank started on port 9999");
                while (true) {
                    Socket client = server.accept();
                    handle(client);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handle(Socket client) {
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            int length = in.readShort();
            byte[] data = new byte[length];
            in.readFully(data);

            IsoMessage request = isoMessageFactory.parseMessage(data, 0);
            
            if (request != null) {
                System.out.println("Received: " + request.debugString());
                
                IsoMessage response = isoMessageFactory.createResponse(request);
                response.setType(0x210);
                response.setValue(39, "00", IsoType.ALPHA, 2);
                
                response.write(client.getOutputStream(), 2);
            }
            client.close();
        } catch (Exception e) {
            
        }
    }
}