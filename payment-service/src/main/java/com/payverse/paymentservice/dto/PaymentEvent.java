package com.payverse.paymentservice.dto;

import java.math.BigDecimal;

public class PaymentEvent {

    private Long senderUserId;
    private Long receiverUserId;
    private BigDecimal amount;
    private String status;

    public PaymentEvent() {
    }

    public PaymentEvent(
            Long senderUserId,
            Long receiverUserId,
            BigDecimal amount,
            String status) {

        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.amount = amount;
        this.status = status;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}