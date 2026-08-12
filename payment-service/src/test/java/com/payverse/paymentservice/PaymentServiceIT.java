package com.payverse.paymentservice;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.event.PaymentEventPublisher;
import com.payverse.paymentservice.service.impl.PaymentServiceImpl;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PaymentServiceIT {

    @MockBean
    private WalletClient walletClient;

    @MockBean
    private PaymentEventPublisher paymentEventPublisher;

    @Autowired
    private PaymentServiceImpl paymentService;


    // =========================================================
    // TEST 1: SUCCESSFUL PAYMENT
    // =========================================================

    @Test
    void transfer_success_should_debit_sender_credit_receiver_and_publish_success() {

        // Arrange
        Long senderUserId = 1L;
        Long receiverUserId = 2L;

        BigDecimal amount = new BigDecimal("100.00");

        // Act
        paymentService.transfer(
                senderUserId,
                receiverUserId,
                amount
        );

        // Assert 1: Sender wallet was debited
        verify(walletClient, times(1)).debit(
                eq(senderUserId),
                eq(amount),
                any(String.class)
        );

        // Assert 2: Receiver wallet was credited
        verify(walletClient, times(1)).addMoney(
                eq(receiverUserId),
                eq(amount),
                any(String.class)
        );

        // Assert 3: Success event was published
        verify(paymentEventPublisher, times(1))
                .publishPaymentSuccess(any());
    }


    // =========================================================
    // TEST 2: RECEIVER CREDIT FAILURE
    // =========================================================

    @Test
    void transfer_receiver_credit_failure_should_publish_failed_event() {

        // Arrange
        Long senderUserId = 1L;
        Long receiverUserId = 2L;

        BigDecimal amount = new BigDecimal("100.00");

        // Simulate receiver wallet failure
        when(walletClient.addMoney(
                eq(receiverUserId),
                eq(amount),
                any(String.class)
        )).thenThrow(
                new RuntimeException("Receiver wallet failed")
        );

        // Act + Assert
        assertThrows(
                RuntimeException.class,
                () -> paymentService.transfer(
                        senderUserId,
                        receiverUserId,
                        amount
                )
        );

        // Assert 1: Sender was debited
        verify(walletClient, times(1)).debit(
                eq(senderUserId),
                eq(amount),
                any(String.class)
        );

        // Assert 2: Receiver credit was attempted
        verify(walletClient, times(1)).addMoney(
                eq(receiverUserId),
                eq(amount),
                any(String.class)
        );

        // Assert 3: Failure event was published
        verify(paymentEventPublisher, times(1))
                .publishPaymentFailed(any());

        // Assert 4: Success event must NOT be published
        verify(paymentEventPublisher, times(0))
                .publishPaymentSuccess(any());
    }
}