package com.payverse.walletservice.controller;

import com.payverse.walletservice.dto.CreateWalletRequest;
import com.payverse.walletservice.dto.WalletResponse;
import com.payverse.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.payverse.walletservice.dto.AddMoneyRequest;
import org.springframework.http.ResponseEntity;


import java.math.BigDecimal;

@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request.getUserId());
    }

    @GetMapping("/{userId}/balance")
    public BigDecimal getBalance(@PathVariable Long userId) {
        return walletService.getBalance(userId);
    }

    @PostMapping("/add-money")
    public ResponseEntity<WalletResponse> addMoney(
        @Valid @RequestBody AddMoneyRequest request) {

    WalletResponse response = walletService.addMoneyIntent(
            request.getUserId(),
            request.getAmount(),
            request.getIdempotencyKey()
    );

    return ResponseEntity.ok(response);
}

@PostMapping("/debit")
public ResponseEntity<WalletResponse> debitMoney(
        @Valid @RequestBody AddMoneyRequest request) {

    WalletResponse response = walletService.debitMoney(
            request.getUserId(),
            request.getAmount()
    );

    return ResponseEntity.ok(response);
}

@PostMapping("/credit")
public ResponseEntity<WalletResponse> creditMoney(
        @Valid @RequestBody AddMoneyRequest request) {

    WalletResponse response = walletService.creditMoney(
            request.getUserId(),
            request.getAmount(),
            request.getIdempotencyKey()
    );

    return ResponseEntity.ok(response);
}
}