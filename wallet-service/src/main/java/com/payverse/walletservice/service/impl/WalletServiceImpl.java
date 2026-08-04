package com.payverse.walletservice.service.impl;

import com.payverse.walletservice.entity.Wallet;
import com.payverse.walletservice.dto.WalletResponse;
import com.payverse.walletservice.repository.WalletRepository;
import com.payverse.walletservice.service.WalletService;
import org.springframework.stereotype.Service;
import com.payverse.walletservice.exception.WalletNotFoundException;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public WalletResponse createWallet(Long userId) {
        if (walletRepository.findByUserId(userId).isPresent()) {
    throw new RuntimeException("Wallet already exists for user: " + userId);
}
Wallet wallet = new Wallet();

wallet.setUserId(userId);
wallet.setBalance(BigDecimal.ZERO);

Wallet savedWallet = walletRepository.save(wallet);

WalletResponse response = new WalletResponse();

response.setWalletId(savedWallet.getId());
response.setUserId(savedWallet.getUserId());
response.setBalance(savedWallet.getBalance());

return response;
    }

  @Override
public BigDecimal getBalance(Long userId) {

    Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() ->
                    new WalletNotFoundException(
                            "Wallet not found for user: " + userId
                    )
            );

    return wallet.getBalance();
    }
}