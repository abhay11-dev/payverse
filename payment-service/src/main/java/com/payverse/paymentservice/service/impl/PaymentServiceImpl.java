package com.payverse.paymentservice.service.impl;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.dto.PaymentEvent;
import com.payverse.paymentservice.service.PaymentService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";

    private final WalletClient walletClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentServiceImpl(
            WalletClient walletClient,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.walletClient = walletClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void transfer(
            Long senderUserId,
            Long receiverUserId,
            BigDecimal amount) {

        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException(
                    "Sender and receiver cannot be the same"
            );
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        String transactionId =
                "payment-" + System.currentTimeMillis();

        // 1. Debit sender
        walletClient.debit(
                senderUserId,
                amount,
                transactionId + "-debit"
        );

        // 2. Credit receiver
        walletClient.addMoney(
                receiverUserId,
                amount,
                transactionId + "-credit"
        );

        // 3. Publish successful payment event
        PaymentEvent event = new PaymentEvent(
                senderUserId,
                receiverUserId,
                amount,
                "PAYMENT_SUCCESS"
        );

       String eventJson = """
        {
          "senderUserId": %d,
          "receiverUserId": %d,
          "amount": %s,
          "status": "PAYMENT_SUCCESS"
        }
        """.formatted(
        senderUserId,
        receiverUserId,
        amount
);

kafkaTemplate.send(
        PAYMENT_EVENTS_TOPIC,
        transactionId,
        eventJson
);

        System.out.println(
                "Payment successful: " + transactionId
        );
    }
}