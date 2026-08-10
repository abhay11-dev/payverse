package com.payverse.paymentservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {

    @KafkaListener(
            topics = "payment-events",
            groupId = "payment-service-group"
    )
    public void consume(String message) {

        System.out.println(
                "Payment event received: " + message
        );
    }
}
