-- Create a basic users table as an example
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, username),
    UNIQUE(tenant_id, email)
);

-- Create an organizations table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    subscription_plan VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
