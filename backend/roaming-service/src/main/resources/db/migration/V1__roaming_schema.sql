-- Roaming Service Schema (OCPI partners registry)
CREATE TABLE IF NOT EXISTS roaming_partners (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    party_id VARCHAR(3) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    token VARCHAR(500),
    versions_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(party_id, country_code)
);
