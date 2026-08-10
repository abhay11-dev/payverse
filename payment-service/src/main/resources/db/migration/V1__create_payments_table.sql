CREATE TABLE payments (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    transaction_id VARCHAR(100) NOT NULL UNIQUE,

    sender_user_id BIGINT NOT NULL,

    receiver_user_id BIGINT NOT NULL,

    amount DECIMAL(19,4) NOT NULL,

    status VARCHAR(50) NOT NULL,

    version BIGINT DEFAULT 0,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);