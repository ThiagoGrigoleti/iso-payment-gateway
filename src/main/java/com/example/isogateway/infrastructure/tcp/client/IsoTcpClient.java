package com.example.isogateway.infrastructure.tcp.client;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

@Component
@RequiredArgsConstructor
public class IsoTcpClient {

    private final MessageFactory<IsoMessage> isoMessageFactory;

    public IsoMessage send(IsoMessage message) throws IOException, java.text.ParseException {

        try (Socket socket = new Socket("localhost", 9999)) {

            message.write(socket.getOutputStream(), 2);


            DataInputStream in = new DataInputStream(socket.getInputStream());


            if (socket.isConnected()) {
                int length = in.readShort();
                byte[] data = new byte[length];
                in.readFully(data);

                return isoMessageFactory.parseMessage(data, 0);
            }
            return null;
        }
    }
}