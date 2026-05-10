-- Auth Service Schema
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    password_hash VARCHAR(500),
    keycloak_id VARCHAR(200) UNIQUE,
    role VARCHAR(20) DEFAULT 'DRIVER',
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    profile_photo VARCHAR(500),
    preferred_lang VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rfid_cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    uid VARCHAR(50) UNIQUE NOT NULL,
    label VARCHAR(100),
    active BOOLEAN DEFAULT TRUE,
    issued_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    make VARCHAR(100),
    model VARCHAR(100),
    year INTEGER,
    registration_number VARCHAR(20),
    battery_capacity_kwh DOUBLE PRECISION,
    connector_types TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(1000) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_rfid_uid ON rfid_cards(uid);

-- Default admin user (password: Admin@123)
INSERT INTO users (id, email, full_name, password_hash, role, is_active, email_verified)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@evroaminghub.in',
    'EV Admin',
    '$2a$12$LMOVhqGh0tFEaHZ3l5I0guBLGMBhFkRMzpFNNvJi3mNJxGqkKubyW',
    'ADMIN',
    true,
    true
) ON CONFLICT DO NOTHING;
