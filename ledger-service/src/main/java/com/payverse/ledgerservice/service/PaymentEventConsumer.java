package com.payverse.ledgerservice.service;

import com.payverse.ledgerservice.event.PaymentEvent;
import com.payverse.ledgerservice.model.LedgerEntry;
import com.payverse.ledgerservice.model.LedgerEntryType;
import com.payverse.ledgerservice.repository.LedgerEntryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentEventConsumer {

    private final LedgerEntryRepository ledgerEntryRepository;

    public PaymentEventConsumer(
            LedgerEntryRepository ledgerEntryRepository) {

        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @KafkaListener(
            topics = "payment-events",
            groupId = "ledger-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(PaymentEvent event) {

        System.out.println(
                "Ledger received payment event: "
                        + event.getTransactionId()
                        + " | type="
                        + event.getEventType()
                        + " | amount="
                        + event.getAmount()
        );

        // Only successful payments create ledger entries
        if (!"PAYMENT_SUCCESS".equals(event.getEventType())) {
            return;
        }

        // 1. DEBIT sender
        LedgerEntry debitEntry =
                new LedgerEntry(
                        event.getSenderWalletId(),
                        LedgerEntryType.DEBIT,
                        event.getAmount(),
                        event.getSenderBalanceAfter(),
                        event.getTransactionId()
                );

        // 2. CREDIT receiver
        LedgerEntry creditEntry =
                new LedgerEntry(
                        event.getReceiverWalletId(),
                        LedgerEntryType.CREDIT,
                        event.getAmount(),
                        event.getReceiverBalanceAfter(),
                        event.getTransactionId()
                );

        // 3. Save both entries
        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        System.out.println(
                "Ledger entries saved: DEBIT + CREDIT"
                        + " | refId=" + event.getTransactionId()
        );
    }
}