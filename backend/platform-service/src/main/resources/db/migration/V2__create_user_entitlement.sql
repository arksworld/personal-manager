CREATE TABLE user_entitlement (
                                  id UUID PRIMARY KEY,
                                  user_subject VARCHAR(255) NOT NULL,
                                  feature_module_id UUID NOT NULL,
                                  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                                  valid_from TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  valid_until TIMESTAMPTZ,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_entitlement_feature
                                      FOREIGN KEY (feature_module_id)
                                          REFERENCES feature_module(id),

                                  CONSTRAINT uk_entitlement_user_feature
                                      UNIQUE (user_subject, feature_module_id),

                                  CONSTRAINT chk_entitlement_status
                                      CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REVOKED')),

                                  CONSTRAINT chk_entitlement_validity
                                      CHECK (valid_until IS NULL OR valid_until > valid_from)
);

CREATE INDEX idx_entitlement_user_status
    ON user_entitlement (user_subject, status);

CREATE INDEX idx_entitlement_feature
    ON user_entitlement (feature_module_id);