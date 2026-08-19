package com.payverse.ledgerservice.dto;

import java.math.BigDecimal;

public class AdminStatsResponse {

    private BigDecimal totalTransactionVolumeToday;
    private long transactionsLastHour;

    public AdminStatsResponse(
            BigDecimal totalTransactionVolumeToday,
            long transactionsLastHour) {

        this.totalTransactionVolumeToday = totalTransactionVolumeToday;
        this.transactionsLastHour = transactionsLastHour;
    }

    public BigDecimal getTotalTransactionVolumeToday() {
        return totalTransactionVolumeToday;
    }

    public long getTransactionsLastHour() {
        return transactionsLastHour;
    }
}
