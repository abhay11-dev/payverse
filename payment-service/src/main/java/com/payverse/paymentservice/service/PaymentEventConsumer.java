package com.payverse.paymentservice.service;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.event.PaymentEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PaymentEventConsumer {

    private final WalletClient walletClient;
    private final StringRedisTemplate redisTemplate;

    public PaymentEventConsumer(
            WalletClient walletClient,
            StringRedisTemplate redisTemplate) {

        this.walletClient = walletClient;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "payment-compensation-group"
    )
    public void handlePaymentFailed(PaymentEvent event) {

        String key =
                "payment:compensation:" + event.getTransactionId();

        Boolean firstProcessing =
                redisTemplate.opsForValue().setIfAbsent(
                        key,
                        "PROCESSING",
                        Duration.ofHours(24)
                );

        if (Boolean.FALSE.equals(firstProcessing)) {

            System.out.println(
                    "Duplicate compensation ignored: "
                            + event.getTransactionId()
            );

            return;
        }

        System.out.println(
                "Received PAYMENT_FAILED: "
                        + event.getTransactionId()
        );

        try {

            // Reverse the original sender debit.
            walletClient.credit(
                    event.getSenderWalletId(),
                    event.getAmount(),
                    event.getTransactionId() + "-compensation"
            );

            redisTemplate.opsForValue().set(
                    key,
                    "COMPLETED",
                    Duration.ofHours(24)
            );

            System.out.println(
                    "Compensation successful: "
                            + event.getTransactionId()
            );

        } catch (Exception exception) {

            // Allow Kafka to retry this event.
            redisTemplate.delete(key);

            System.out.println(
                    "Compensation failed. Kafka will retry: "
                            + event.getTransactionId()
            );

            throw exception;
        }
    }
}