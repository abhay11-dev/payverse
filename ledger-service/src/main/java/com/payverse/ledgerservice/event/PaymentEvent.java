package com.payverse.ledgerservice.event;

import java.math.BigDecimal;

public class PaymentEvent {

    private String transactionId;

    private Long senderUserId;
    private Long receiverUserId;

    private Long senderWalletId;
    private Long receiverWalletId;

    private BigDecimal amount;

    private BigDecimal senderBalanceAfter;
    private BigDecimal receiverBalanceAfter;

    private String eventType;

    public PaymentEvent() {
    }

    public PaymentEvent(
            String transactionId,
            Long senderUserId,
            Long receiverUserId,
            Long senderWalletId,
            Long receiverWalletId,
            BigDecimal amount,
            BigDecimal senderBalanceAfter,
            BigDecimal receiverBalanceAfter,
            String eventType) {

        this.transactionId = transactionId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.senderBalanceAfter = senderBalanceAfter;
        this.receiverBalanceAfter = receiverBalanceAfter;
        this.eventType = eventType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public Long getSenderWalletId() {
        return senderWalletId;
    }

    public Long getReceiverWalletId() {
        return receiverWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getSenderBalanceAfter() {
        return senderBalanceAfter;
    }

    public BigDecimal getReceiverBalanceAfter() {
        return receiverBalanceAfter;
    }

    public String getEventType() {
        return eventType;
    }
}