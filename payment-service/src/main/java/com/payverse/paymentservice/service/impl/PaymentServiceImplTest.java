package com.payverse.paymentservice.service.impl;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.event.PaymentEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private WalletClient walletClient;
    private PaymentEventPublisher paymentEventPublisher;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        walletClient = mock(WalletClient.class);
        paymentEventPublisher = mock(PaymentEventPublisher.class);

        paymentService = new PaymentServiceImpl(
                walletClient,
                paymentEventPublisher
        );
    }

    @Test
    void transfer_success() {

        Long senderUserId = 1L;
        Long receiverUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        paymentService.transfer(
                senderUserId,
                receiverUserId,
                amount
        );

        verify(walletClient, times(1)).debit(
                eq(senderUserId),
                eq(amount),
                any(String.class)
        );

        verify(walletClient, times(1)).addMoney(
                eq(receiverUserId),
                eq(amount),
                any(String.class)
        );

        verify(paymentEventPublisher, times(1))
                .publishPaymentSuccess(any());
    }

    @Test
    void transfer_should_fail_when_sender_and_receiver_are_same() {

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transfer(
                        1L,
                        1L,
                        new BigDecimal("100.00")
                )
        );

        verifyNoInteractions(walletClient);
        verifyNoInteractions(paymentEventPublisher);
    }

    @Test
    void transfer_should_fail_when_amount_is_null() {

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transfer(
                        1L,
                        2L,
                        null
                )
        );

        verifyNoInteractions(walletClient);
        verifyNoInteractions(paymentEventPublisher);
    }

    @Test
    void transfer_should_fail_when_amount_is_zero() {

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transfer(
                        1L,
                        2L,
                        BigDecimal.ZERO
                )
        );

        verifyNoInteractions(walletClient);
        verifyNoInteractions(paymentEventPublisher);
    }

    @Test
    void transfer_should_fail_when_amount_is_negative() {

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transfer(
                        1L,
                        2L,
                        new BigDecimal("-10.00")
                )
        );

        verifyNoInteractions(walletClient);
        verifyNoInteractions(paymentEventPublisher);
    }

    @Test
    void transfer_should_publish_failed_event_when_receiver_credit_fails() {

        Long senderUserId = 1L;
        Long receiverUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        doThrow(new RuntimeException("Receiver wallet unavailable"))
                .when(walletClient)
                .addMoney(
                        eq(receiverUserId),
                        eq(amount),
                        any(String.class)
                );

        assertThrows(
                RuntimeException.class,
                () -> paymentService.transfer(
                        senderUserId,
                        receiverUserId,
                        amount
                )
        );

        verify(walletClient, times(1)).debit(
                eq(senderUserId),
                eq(amount),
                any(String.class)
        );

        verify(walletClient, times(1)).addMoney(
                eq(receiverUserId),
                eq(amount),
                any(String.class)
        );

        verify(paymentEventPublisher, times(1))
                .publishPaymentFailed(any());

        verify(paymentEventPublisher, never())
                .publishPaymentSuccess(any());
    }
}