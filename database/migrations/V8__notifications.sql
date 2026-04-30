-- ============================================================
-- V8: Notifications
-- ============================================================

CREATE TABLE notification_templates (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('EMAIL', 'SMS', 'PUSH')),
    subject     VARCHAR(255),
    body        TEXT NOT NULL,
    variables   TEXT[],
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Pre-populate templates
INSERT INTO notification_templates (name, type, subject, body, variables) VALUES
('SESSION_STARTED_EMAIL', 'EMAIL', 'Charging Session Started - EV Roaming Hub',
 'Hello {{user_name}}, your charging session has started at {{station_name}}. Transaction ID: {{transaction_id}}.',
 ARRAY['user_name', 'station_name', 'transaction_id']),
('SESSION_COMPLETED_EMAIL', 'EMAIL', 'Charging Complete - {{energy_kwh}} kWh | ₹{{amount}}',
 'Hello {{user_name}}, your session at {{station_name}} is complete. Energy: {{energy_kwh}} kWh. Amount: ₹{{amount}}. Invoice: {{invoice_number}}.',
 ARRAY['user_name', 'station_name', 'energy_kwh', 'amount', 'invoice_number']),
('PAYMENT_SUCCESS_EMAIL', 'EMAIL', 'Payment Confirmed ₹{{amount}}',
 'Hello {{user_name}}, payment of ₹{{amount}} received. Reference: {{payment_id}}.',
 ARRAY['user_name', 'amount', 'payment_id']),
('SESSION_STARTED_PUSH', 'PUSH', NULL,
 '⚡ Charging started at {{station_name}}. Session ID: {{transaction_id}}',
 ARRAY['station_name', 'transaction_id']),
('SESSION_COMPLETED_PUSH', 'PUSH', NULL,
 '✅ Charged {{energy_kwh}} kWh for ₹{{amount}} at {{station_name}}',
 ARRAY['energy_kwh', 'amount', 'station_name']),
('SESSION_STARTED_SMS', 'SMS', NULL,
 'EV Hub: Charging started at {{station_name}}. TxID: {{transaction_id}}. Reply STOP to opt out.',
 ARRAY['station_name', 'transaction_id']);

-- Notification Log
CREATE TABLE notification_log (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    template_id     UUID REFERENCES notification_templates(id),
    channel         VARCHAR(10) NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'PUSH')),
    recipient       VARCHAR(255) NOT NULL,     -- email/phone/device token
    subject         VARCHAR(255),
    body            TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    reference_id    UUID,                      -- session_id, payment_id, etc.
    reference_type  VARCHAR(50),
    provider        VARCHAR(50),               -- sendgrid, fcm, twilio
    provider_msg_id VARCHAR(255),
    failure_reason  TEXT,
    retry_count     SMALLINT DEFAULT 0,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_log_user_id    ON notification_log(user_id);
CREATE INDEX idx_notif_log_status     ON notification_log(status);
CREATE INDEX idx_notif_log_created_at ON notification_log(created_at DESC);
CREATE INDEX idx_notif_log_reference  ON notification_log(reference_id);

-- Push Device Tokens
CREATE TABLE device_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       TEXT NOT NULL,
    platform    VARCHAR(10) NOT NULL CHECK (platform IN ('IOS', 'ANDROID', 'WEB')),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);

CREATE TRIGGER trigger_device_tokens_updated_at
    BEFORE UPDATE ON device_tokens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
