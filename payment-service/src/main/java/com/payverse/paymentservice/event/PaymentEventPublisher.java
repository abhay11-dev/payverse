package com.payverse.paymentservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private static final String PAYMENT_EVENTS_TOPIC =
            "payment-events";

    private static final String PAYMENT_FAILED_TOPIC =
            "payment-failed";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventPublisher(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccess(PaymentEvent event) {

        kafkaTemplate.send(
                PAYMENT_EVENTS_TOPIC,
                event.getTransactionId(),
                event
        );

        System.out.println(
                "Published PAYMENT_SUCCESS: "
                        + event.getTransactionId()
        );
    }

    public void publishPaymentFailed(PaymentEvent event) {

        kafkaTemplate.send(
                PAYMENT_FAILED_TOPIC,
                event.getTransactionId(),
                event
        );

        System.out.println(
                "Published PAYMENT_FAILED: "
                        + event.getTransactionId()
        );
    }
}