package com.payverse.walletservice.service.impl;

import com.payverse.walletservice.dto.WalletResponse;
import com.payverse.walletservice.entity.Wallet;
import com.payverse.walletservice.repository.WalletRepository;
import com.payverse.walletservice.service.WalletService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration IDEMPOTENCY_TTL =
            Duration.ofHours(24);

    public WalletServiceImpl(
            WalletRepository walletRepository,
            RedisTemplate<String, Object> redisTemplate) {

        this.walletRepository = walletRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public WalletResponse createWallet(Long userId) {

        if (walletRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException(
                    "Wallet already exists for user: " + userId
            );
        }

        Wallet wallet = new Wallet();

        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setVersion(0L);

        Wallet savedWallet = walletRepository.save(wallet);

        return toResponse(savedWallet);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found for user: " + userId
                        )
                );

        return wallet.getBalance();
    }

    @Override
    @Transactional
    public WalletResponse addMoneyIntent(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        validateAmount(amount);

        String redisKey = "wallet:idempotency:" + idempotencyKey;

        Object existingResult = redisTemplate.opsForValue()
                .get(redisKey);

        if (existingResult != null) {
            throw new IllegalStateException(
                    "Duplicate request. Idempotency key already used: "
                            + idempotencyKey
            );
        }

        redisTemplate.opsForValue()
                .set(
                        redisKey,
                        "PROCESSING",
                        IDEMPOTENCY_TTL
                );

        try {

            Wallet wallet = getWallet(userId);

            wallet.setBalance(
                    wallet.getBalance().add(amount)
            );

            Wallet savedWallet = walletRepository.save(wallet);

            WalletResponse response = toResponse(savedWallet);

            redisTemplate.opsForValue()
                    .set(
                            redisKey,
                            response,
                            IDEMPOTENCY_TTL
                    );

            return response;

        } catch (Exception exception) {

            redisTemplate.delete(redisKey);

            throw exception;
        }
    }

    @Override
    @Transactional
    public WalletResponse debitMoney(
            Long userId,
            BigDecimal amount) {

        validateAmount(amount);

        Wallet wallet = getWallet(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient wallet balance"
            );
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount)
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return toResponse(savedWallet);
    }

    @Override
    @Transactional
    public WalletResponse creditMoney(
            Long userId,
            BigDecimal amount,
            String idempotencyKey) {

        validateAmount(amount);

        String redisKey =
                "wallet:credit:" + idempotencyKey;

        Object existingResult =
                redisTemplate.opsForValue().get(redisKey);

        if (existingResult != null) {
            throw new IllegalStateException(
                    "Duplicate credit request. Idempotency key already used: "
                            + idempotencyKey
            );
        }

        redisTemplate.opsForValue()
                .set(
                        redisKey,
                        "PROCESSING",
                        IDEMPOTENCY_TTL
                );

        try {

            Wallet wallet = getWallet(userId);

            wallet.setBalance(
                    wallet.getBalance().add(amount)
            );

            Wallet savedWallet =
                    walletRepository.save(wallet);

            WalletResponse response =
                    toResponse(savedWallet);

            redisTemplate.opsForValue()
                    .set(
                            redisKey,
                            response,
                            IDEMPOTENCY_TTL
                    );

            return response;

        } catch (Exception exception) {

            redisTemplate.delete(redisKey);

            throw exception;
        }
    }

    private Wallet getWallet(Long userId) {

        return walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Wallet not found for user: " + userId
                        )
                );
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }

    private WalletResponse toResponse(Wallet wallet) {

        WalletResponse response = new WalletResponse();

        response.setWalletId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());

        return response;
    }
}
