-- ============================================================
-- V13: Fraud Detection Service Schema
-- EV Roaming Hub India
-- ============================================================

CREATE TABLE IF NOT EXISTS fraud_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name       VARCHAR(100) NOT NULL UNIQUE,
    rule_type       VARCHAR(50) NOT NULL,  -- ENERGY, DURATION, RATE, PAYMENT, ROAMING
    description     TEXT,
    threshold_value DECIMAL(15,4),
    threshold_unit  VARCHAR(30),
    severity        VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fraud_alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      VARCHAR(100) NOT NULL,
    user_id         VARCHAR(100),
    station_id      VARCHAR(100),
    token_id        VARCHAR(100),
    alert_type      VARCHAR(100) NOT NULL,
    description     TEXT NOT NULL,
    severity        VARCHAR(20) NOT NULL,  -- LOW, MEDIUM, HIGH, CRITICAL
    status          VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    resolved_by     VARCHAR(100),
    resolution_notes TEXT,
    resolved_at     TIMESTAMPTZ,
    detected_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fraud_rule_violations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alert_id        UUID NOT NULL REFERENCES fraud_alerts(id) ON DELETE CASCADE,
    rule_id         UUID REFERENCES fraud_rules(id),
    rule_name       VARCHAR(100) NOT NULL,
    actual_value    DECIMAL(15,4),
    threshold_value DECIMAL(15,4),
    violated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Seed default rules
INSERT INTO fraud_rules (rule_name, rule_type, description, threshold_value, threshold_unit, severity)
VALUES
    ('ABNORMAL_ENERGY',       'ENERGY',   'Session energy exceeds realistic maximum',   150.0,   'kWh',     'HIGH'),
    ('IMPOSSIBLE_DURATION',   'DURATION', 'DC session exceeds 6 hours',                  360.0,   'minutes', 'MEDIUM'),
    ('RAPID_SESSION_RATE',    'RATE',     'Same token used 5+ times per hour',             5.0,   'per_hour','HIGH'),
    ('ZERO_ENERGY_SESSION',   'ENERGY',   'Session ran but delivered zero energy',          0.0,   'kWh',     'MEDIUM'),
    ('HIGH_VALUE_PAYMENT',    'PAYMENT',  'Single payment exceeds Rs.50,000',          50000.0,   'INR',     'HIGH'),
    ('ROAMING_GEO_ANOMALY',   'ROAMING',  'Same token used across distant geographies',    2.0,   'regions', 'CRITICAL')
ON CONFLICT (rule_name) DO NOTHING;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_session    ON fraud_alerts(session_id);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_user       ON fraud_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_status     ON fraud_alerts(status, severity);
CREATE INDEX IF NOT EXISTS idx_fraud_alerts_detected   ON fraud_alerts(detected_at DESC);
