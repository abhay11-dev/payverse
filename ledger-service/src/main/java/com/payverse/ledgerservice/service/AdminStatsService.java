package com.payverse.ledgerservice.service;

import com.payverse.ledgerservice.dto.AdminStatsResponse;
import com.payverse.ledgerservice.model.LedgerEntryType;
import com.payverse.ledgerservice.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AdminStatsService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public AdminStatsService(
            LedgerEntryRepository ledgerEntryRepository) {

        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public AdminStatsResponse getStats() {

        LocalDateTime startOfDay =
                LocalDate.now().atStartOfDay();

        LocalDateTime oneHourAgo =
                LocalDateTime.now().minusHours(1);

        BigDecimal totalVolume =
                ledgerEntryRepository.getTransactionVolumeToday(
                        LedgerEntryType.DEBIT,
                        startOfDay
                );

        long transactionsLastHour =
                ledgerEntryRepository.countTransactionsLastHour(
                        LedgerEntryType.DEBIT,
                        oneHourAgo
                );

        return new AdminStatsResponse(
                totalVolume,
                transactionsLastHour
        );
    }
}
