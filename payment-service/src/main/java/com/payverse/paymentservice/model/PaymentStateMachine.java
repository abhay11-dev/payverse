package com.payverse.paymentservice.model;

public final class PaymentStateMachine {

    private PaymentStateMachine() {
        // Utility class
    }

    public static PaymentStatus transition(
            PaymentStatus current,
            PaymentStatus next) {

        if (current == null || next == null) {
            throw new IllegalStateException(
                    "Payment status cannot be null"
            );
        }

        boolean valid = switch (current) {

            case INITIATED ->
                    next == PaymentStatus.PROCESSING;

            case PROCESSING ->
                    next == PaymentStatus.SUCCESS
                            || next == PaymentStatus.FAILED;

            default ->
                    false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid payment transition: "
                            + current + " -> " + next
            );
        }

        return next;
    }
}
