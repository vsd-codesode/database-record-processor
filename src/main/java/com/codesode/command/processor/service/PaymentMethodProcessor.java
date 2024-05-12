package com.codesode.command.processor.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class PaymentMethodProcessor {

    public Message<?> processRecord(Message<?> message) {
        //TODO bring your code here from**Helper class

        ProcessingStatus status;
        try {
            Thread.sleep(1000);
            status = ProcessingStatus.randomStatus();
        } catch (Exception e) {
            status = ProcessingStatus.FAILED;
            throw new RuntimeException(e);
        }


        //this will be written on the output csv file
        return MessageBuilder.withPayload(message.getPayload())
                .copyHeaders(message.getHeaders())
                .setHeader("processingStatus", status.name())
                .build();
    }


    public static enum ProcessingStatus {
        DONE,
        FAILED;

        private static final int SIZE = values().length;

        public static ProcessingStatus randomStatus() {
            return values()[ThreadLocalRandom.current().nextInt(SIZE)];
        }
    }
}
