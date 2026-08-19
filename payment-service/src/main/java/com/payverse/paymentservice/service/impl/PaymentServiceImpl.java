package com.payverse.paymentservice.service.impl;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.dto.WalletResponse;
import com.payverse.paymentservice.event.PaymentEvent;
import com.payverse.paymentservice.event.PaymentEventPublisher;
import com.payverse.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final WalletClient walletClient;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentServiceImpl(
            WalletClient walletClient,
            PaymentEventPublisher paymentEventPublisher) {

        this.walletClient = walletClient;
        this.paymentEventPublisher = paymentEventPublisher;
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
        WalletResponse senderWallet =
                walletClient.debit(
                        senderUserId,
                        amount,
                        transactionId + "-debit"
                );

        System.out.println(
                "Sender debited successfully: "
                        + senderUserId
                        + " | balanceAfter="
                        + senderWallet.getBalance()
        );

        // 2. Credit receiver
        try {

            WalletResponse receiverWallet =
                    walletClient.addMoney(
                            receiverUserId,
                            amount,
                            transactionId + "-credit"
                    );

            System.out.println(
                    "Receiver credited successfully: "
                            + receiverUserId
                            + " | balanceAfter="
                            + receiverWallet.getBalance()
            );

            // 3. Create successful payment event
            PaymentEvent successEvent =
                    new PaymentEvent(
                            transactionId,
                            senderUserId,
                            receiverUserId,
                            senderWallet.getWalletId(),
                            receiverWallet.getWalletId(),
                            amount,
                            senderWallet.getBalance(),
                            receiverWallet.getBalance(),
                            "PAYMENT_SUCCESS"
                    );

            // 4. Publish successful payment event
            paymentEventPublisher.publishPaymentSuccess(
                    successEvent
            );

            System.out.println(
                    "Payment successful: "
                            + transactionId
            );

        } catch (Exception creditException) {

            // Receiver credit failed
            PaymentEvent failedEvent =
                    new PaymentEvent(
                            transactionId,
                            senderUserId,
                            receiverUserId,
                            null,
                            null,
                            amount,
                            null,
                            null,
                            "PAYMENT_FAILED"
                    );

            paymentEventPublisher.publishPaymentFailed(
                    failedEvent
            );

            System.out.println(
                    "PAYMENT_FAILED published for: "
                            + transactionId
            );

            throw new RuntimeException(
                    "Receiver credit failed. "
                            + "Compensation event published.",
                    creditException
            );
        }
    }
}