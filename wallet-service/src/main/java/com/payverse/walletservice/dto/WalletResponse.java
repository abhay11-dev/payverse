package com.payverse.walletservice.dto;

import java.math.BigDecimal;
import java.io.Serializable;

public class WalletResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long walletId;
    private Long userId;
    private BigDecimal balance;

    public WalletResponse() {
    }

    public WalletResponse(Long walletId, Long userId, BigDecimal balance) {
        this.walletId = walletId;
        this.userId = userId;
        this.balance = balance;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}