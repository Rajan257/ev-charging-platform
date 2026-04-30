-- ============================================================
-- V1: Users, Auth, RFID Cards, Roles
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Roles
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO roles (name, description) VALUES
    ('DRIVER',         'EV vehicle driver / end user'),
    ('CPO_ADMIN',      'Charge Point Operator admin'),
    ('PLATFORM_ADMIN', 'EV Roaming Hub platform administrator'),
    ('MSP_ADMIN',      'Mobility Service Provider admin');

-- Users
CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email            VARCHAR(255) UNIQUE NOT NULL,
    phone            VARCHAR(20) UNIQUE,
    full_name        VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(255),                -- NULL if SSO only
    keycloak_id      VARCHAR(255) UNIQUE,
    role             VARCHAR(50) NOT NULL DEFAULT 'DRIVER' REFERENCES roles(name),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    profile_photo    TEXT,
    preferred_lang   VARCHAR(10) DEFAULT 'en',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_phone       ON users(phone);

-- RFID Cards
CREATE TABLE rfid_cards (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    uid          VARCHAR(100) UNIQUE NOT NULL,   -- physical card UID (hex)
    label        VARCHAR(100),
    card_type    VARCHAR(20) NOT NULL DEFAULT 'RFID' CHECK (card_type IN ('RFID', 'NFC', 'APP')),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    issued_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rfid_uid     ON rfid_cards(uid);
CREATE INDEX idx_rfid_user_id ON rfid_cards(user_id);

-- Linked Vehicles (ISO 15118 / Plug & Charge)
CREATE TABLE vehicles (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    make                VARCHAR(100) NOT NULL,
    model               VARCHAR(100) NOT NULL,
    year                INT,
    registration_number VARCHAR(50),
    vin                 VARCHAR(17) UNIQUE,
    contract_id         VARCHAR(255) UNIQUE,     -- ISO 15118 EMAID
    pnc_certificate     TEXT,                    -- PEM cert for Plug & Charge
    is_default          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_user_id     ON vehicles(user_id);
CREATE INDEX idx_vehicles_contract_id ON vehicles(contract_id);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    device_info TEXT,
    ip_address  INET,
    issued_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at  TIMESTAMPTZ
);

CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);

-- Audit Log
CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id),
    action      VARCHAR(100) NOT NULL,
    resource    VARCHAR(100),
    resource_id UUID,
    details     JSONB,
    ip_address  INET,
    user_agent  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_user_id    ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);

-- Updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
