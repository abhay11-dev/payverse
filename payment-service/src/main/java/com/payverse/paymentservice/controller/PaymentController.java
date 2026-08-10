package com.payverse.paymentservice.controller;

import com.payverse.paymentservice.client.WalletClient;
import com.payverse.paymentservice.dto.WalletResponse;
import org.springframework.web.bind.annotation.*;

import com.payverse.paymentservice.service.PaymentService;
import com.payverse.paymentservice.dto.TransferRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {


    private final WalletClient walletClient;
private final PaymentService paymentService;

public PaymentController(
        WalletClient walletClient,
        PaymentService paymentService) {

    this.walletClient = walletClient;
    this.paymentService = paymentService;
}

    @PostMapping("/test-debit")
    public WalletResponse testDebit(
            @RequestParam Long walletId,
            @RequestParam BigDecimal amount) {

        String idempotencyKey =
                "payment-debit-" + walletId + "-" + System.currentTimeMillis();

        return walletClient.debit(
                walletId,
                amount,
                idempotencyKey
        );
    }


    @PostMapping("/transfer")
public ResponseEntity<String> transfer(
        @RequestBody TransferRequest request) {

    paymentService.transfer(
            request.getSenderUserId(),
            request.getReceiverUserId(),
            request.getAmount()
    );

    return ResponseEntity.ok("Transfer successful");
}
}