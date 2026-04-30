-- ============================================================
-- V6: Roaming Partners, OCPI Tokens, Roaming Sessions
-- ============================================================

-- Roaming Partners (other CPO/MSP hubs)
CREATE TABLE roaming_partners (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(255) NOT NULL,
    party_id            VARCHAR(3) NOT NULL,      -- OCPI party_id (3 chars)
    country_code        VARCHAR(2) NOT NULL DEFAULT 'IN',
    role                VARCHAR(20) NOT NULL DEFAULT 'CPO'
                        CHECK (role IN ('CPO', 'MSP', 'HUB', 'NSP')),
    ocpi_base_url       TEXT NOT NULL,
    token_a             VARCHAR(512),              -- our token sent to them
    token_b             VARCHAR(512),              -- their token sent to us (hashed)
    token_c             VARCHAR(512),              -- negotiated token (hashed)
    auth_status         VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (auth_status IN ('PENDING', 'REGISTERED', 'ACTIVE', 'SUSPENDED', 'REVOKED')),
    business_details    JSONB,
    supported_modules   TEXT[],                   -- locations, sessions, tokens, tariffs, etc.
    contract_start      DATE,
    contract_end        DATE,
    revenue_share_pct   DECIMAL(5, 2) DEFAULT 0,  -- % of revenue to this partner
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_roaming_partners_party ON roaming_partners(party_id, country_code);

CREATE TRIGGER trigger_roaming_partners_updated_at
    BEFORE UPDATE ON roaming_partners
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Backfill session roaming partner FK
ALTER TABLE charging_sessions ADD CONSTRAINT fk_session_roaming_partner
    FOREIGN KEY (roaming_partner_id) REFERENCES roaming_partners(id);

-- OCPI Tokens (driver tokens exchanged between networks)
CREATE TABLE ocpi_tokens (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    uid                 VARCHAR(100) NOT NULL,     -- RFID UID or app token
    token_type          VARCHAR(20) NOT NULL DEFAULT 'RFID'
                        CHECK (token_type IN ('RFID', 'APP_USER', 'AD_HOC_USER', 'OTHER')),
    contract_id         VARCHAR(255),              -- EMAID for PnC
    visual_number       VARCHAR(100),
    issuer              VARCHAR(255) NOT NULL,     -- MSP name
    group_id            VARCHAR(36),
    user_id             UUID REFERENCES users(id),
    roaming_partner_id  UUID REFERENCES roaming_partners(id),
    valid               BOOLEAN NOT NULL DEFAULT TRUE,
    whitelist           VARCHAR(20) DEFAULT 'NEVER'
                        CHECK (whitelist IN ('NEVER', 'EXACTLY_ONCE', 'ALLOWED_OFFLINE', 'ALLOWED')),
    language            VARCHAR(5) DEFAULT 'en',
    last_updated        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ocpi_tokens_uid       ON ocpi_tokens(uid);
CREATE INDEX idx_ocpi_tokens_user_id   ON ocpi_tokens(user_id);
CREATE INDEX idx_ocpi_tokens_partner   ON ocpi_tokens(roaming_partner_id);

-- OCPI Sessions (roaming sessions tracked separately)
CREATE TABLE ocpi_roaming_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id          UUID REFERENCES charging_sessions(id),
    ocpi_session_id     VARCHAR(100) NOT NULL,    -- remote partner's session ID
    roaming_partner_id  UUID NOT NULL REFERENCES roaming_partners(id),
    country_code        VARCHAR(2) NOT NULL,
    party_id            VARCHAR(3) NOT NULL,
    location_id         VARCHAR(100),
    evse_uid            VARCHAR(100),
    connector_id_remote VARCHAR(100),
    start_datetime      TIMESTAMPTZ NOT NULL,
    end_datetime        TIMESTAMPTZ,
    kwh                 DECIMAL(10, 4) DEFAULT 0,
    auth_id             VARCHAR(100),
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'COMPLETED', 'INVALID', 'PENDING')),
    total_cost          DECIMAL(12, 2),
    currency            VARCHAR(3) DEFAULT 'INR',
    last_updated        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ocpi_sessions_partner ON ocpi_roaming_sessions(roaming_partner_id);

-- OCPI CDR (Charge Detail Records for roaming billing)
CREATE TABLE ocpi_cdrs (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cdr_id              VARCHAR(100) NOT NULL UNIQUE,
    roaming_session_id  UUID NOT NULL REFERENCES ocpi_roaming_sessions(id),
    roaming_partner_id  UUID NOT NULL REFERENCES roaming_partners(id),
    total_energy        DECIMAL(10, 4) NOT NULL,
    total_time          DECIMAL(10, 4) NOT NULL,       -- hours
    total_cost          DECIMAL(12, 2) NOT NULL,
    currency            VARCHAR(3) DEFAULT 'INR',
    tariff_id           UUID REFERENCES tariffs(id),
    signed_data         TEXT,                          -- for non-repudiation
    cdr_payload         JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
