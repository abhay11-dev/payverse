CREATE TABLE ledger_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    balance_after DECIMAL(19,4) NOT NULL,
    ref_id VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT chk_ledger_entry_type
        CHECK (type IN ('DEBIT', 'CREDIT'))
);
