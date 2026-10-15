CREATE TABLE vault_item (
                            id UUID PRIMARY KEY,
                            owner_subject VARCHAR(255) NOT NULL,
                            application_name VARCHAR(200) NOT NULL,
                            item_name VARCHAR(200) NOT NULL,
                            encrypted_payload BYTEA NOT NULL,
                            encryption_version VARCHAR(50) NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vault_item_owner
    ON vault_item (owner_subject);