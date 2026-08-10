package com.payverse.paymentservice.service;

import java.math.BigDecimal;

public interface PaymentService {

    void transfer(
            Long senderWalletId,
            Long receiverWalletId,
            BigDecimal amount
    );
}