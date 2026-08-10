package com.payverse.paymentservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentStateMachineTest {

    @Test
    void shouldRejectInitiatedToSuccess() {

        assertThrows(
                IllegalStateException.class,
                () -> PaymentStateMachine.transition(
                        PaymentStatus.INITIATED,
                        PaymentStatus.SUCCESS
                )
        );
    }
}
