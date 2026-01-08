package com.example.isogateway.core.iso;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class IsoConfig {

    @Bean
    public MessageFactory<IsoMessage> messageFactory() throws IOException {
        MessageFactory<IsoMessage> factory = new MessageFactory<>();
        factory.setCharacterEncoding(StandardCharsets.US_ASCII.name());
        factory.setUseBinaryMessages(false);
        factory.setAssignDate(true);

        IsoMessage template0200 = new IsoMessage();
        template0200.setType(0x200);
        template0200.setValue(2, "0000000000000000", IsoType.LLVAR, 0);
        template0200.setValue(3, "000000", IsoType.NUMERIC, 6);
        template0200.setValue(4, "000000000000", IsoType.NUMERIC, 12);
        template0200.setValue(7, "0101000000", IsoType.DATE10, 10);
        template0200.setValue(11, "000000", IsoType.NUMERIC, 6);

        factory.addMessageTemplate(template0200);


        IsoMessage template0210 = new IsoMessage();
        template0210.setType(0x210);
        template0210.setValue(2, "0000000000000000", IsoType.LLVAR, 0);
        template0210.setValue(3, "000000", IsoType.NUMERIC, 6);
        template0210.setValue(4, "000000000000", IsoType.NUMERIC, 12);
        template0210.setValue(7, "0101000000", IsoType.DATE10, 10);
        template0210.setValue(11, "000000", IsoType.NUMERIC, 6);
        template0210.setValue(39, "00", IsoType.ALPHA, 2);

        factory.addMessageTemplate(template0210);

        return factory;
    }
}