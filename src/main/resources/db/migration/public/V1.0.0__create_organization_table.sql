CREATE SEQUENCE IF NOT EXISTS organization_seq
    INCREMENT 50
    START 1;

CREATE TABLE IF NOT EXISTS organizations (
                                             id BIGINT PRIMARY KEY,
                                             name VARCHAR(255) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL UNIQUE,
    subscription_plan VARCHAR(50) NOT NULL
    );

