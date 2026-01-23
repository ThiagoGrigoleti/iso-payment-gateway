CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    card_number_masked VARCHAR(19) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    stan VARCHAR(6) NOT NULL UNIQUE,
    request_mti VARCHAR(4) NOT NULL,
    response_mti VARCHAR(4),
    response_code VARCHAR(2),
    status VARCHAR(20) NOT NULL,
    retrieval_reference_number VARCHAR(12),
    authorization_code VARCHAR(6),
    processing_time_ms BIGINT,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    raw_request VARCHAR(2048),
    raw_response VARCHAR(2048)
);

CREATE INDEX IF NOT EXISTS idx_stan ON transactions(stan);
CREATE INDEX IF NOT EXISTS idx_card_number ON transactions(card_number_masked);
CREATE INDEX IF NOT EXISTS idx_created_at ON transactions(created_at);
