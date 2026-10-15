CREATE TABLE feature_module (
                                id UUID PRIMARY KEY,
                                code VARCHAR(100) NOT NULL UNIQUE,
                                display_name VARCHAR(150) NOT NULL,
                                description TEXT,
                                enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);