package com.payverse.ledgerservice.repository;

import com.payverse.ledgerservice.model.LedgerEntry;
import com.payverse.ledgerservice.model.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId);

    @Query("""
            SELECT COALESCE(SUM(l.amount), 0)
            FROM LedgerEntry l
            WHERE l.type = :type
            AND l.createdAt >= :startOfDay
            """)
    BigDecimal getTransactionVolumeToday(
            LedgerEntryType type,
            LocalDateTime startOfDay
    );

    @Query("""
            SELECT COUNT(DISTINCT l.refId)
            FROM LedgerEntry l
            WHERE l.type = :type
            AND l.createdAt >= :oneHourAgo
            """)
    long countTransactionsLastHour(
            LedgerEntryType type,
            LocalDateTime oneHourAgo
    );
}