package com.payverse.walletservice.service;

import com.payverse.walletservice.dto.WalletResponse;

import java.math.BigDecimal;

public interface WalletService {

    WalletResponse createWallet(Long userId);

    BigDecimal getBalance(Long userId);
}