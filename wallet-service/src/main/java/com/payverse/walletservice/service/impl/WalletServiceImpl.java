package com.payverse.walletservice.service.impl;

import com.payverse.walletservice.entity.Wallet;
import com.payverse.walletservice.dto.WalletResponse;
import com.payverse.walletservice.repository.WalletRepository;
import com.payverse.walletservice.service.WalletService;
import org.springframework.stereotype.Service;
import com.payverse.walletservice.exception.WalletNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;
import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final WalletRepository walletRepository;

   public WalletServiceImpl(
        WalletRepository walletRepository,
        RedisTemplate<String, Object> redisTemplate) {

    this.walletRepository = walletRepository;
    this.redisTemplate = redisTemplate;
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

@Override
public WalletResponse addMoneyIntent(
        Long userId,
        BigDecimal amount,
        String idempotencyKey) {

    String redisKey = "idempotency:" + idempotencyKey;

    Boolean firstRequest = redisTemplate
            .opsForValue()
            .setIfAbsent(
                    redisKey,
                    "processing",
                    Duration.ofHours(24)
            );

    if (Boolean.FALSE.equals(firstRequest)) {

    Object cachedResponse = redisTemplate
            .opsForValue()
            .get(redisKey);

    if (cachedResponse instanceof WalletResponse) { //return if already processed and response is cached
        return (WalletResponse) cachedResponse;
    }

    throw new RuntimeException( //return error if request is already being processed and response is not cached
            "Request is already being processed."
    );
}

   Wallet wallet = walletRepository.findByUserId(userId)
        .orElseThrow(() ->
                new WalletNotFoundException(
                        "Wallet not found for user: " + userId
                )
        );

wallet.setBalance(wallet.getBalance().add(amount));

Wallet updatedWallet = walletRepository.save(wallet);

WalletResponse response = new WalletResponse();
response.setWalletId(updatedWallet.getId());
response.setUserId(updatedWallet.getUserId());
response.setBalance(updatedWallet.getBalance());
redisTemplate.opsForValue().set(
        redisKey,
        response,
        Duration.ofHours(24)
);
return response;
}
}