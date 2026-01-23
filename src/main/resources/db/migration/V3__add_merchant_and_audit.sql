CREATE TABLE IF NOT EXISTS merchants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    api_key_hash VARCHAR(64) NOT NULL UNIQUE,
    api_key_prefix VARCHAR(12) NOT NULL,
    secret_key_hash VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    actor_ip VARCHAR(45),
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE transactions ADD COLUMN IF NOT EXISTS merchant_id BIGINT;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS original_stan VARCHAR(6);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS reversal_stan VARCHAR(6);

CREATE INDEX IF NOT EXISTS idx_merchant_api_key ON merchants(api_key_hash);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_merchant ON transactions(merchant_id);
