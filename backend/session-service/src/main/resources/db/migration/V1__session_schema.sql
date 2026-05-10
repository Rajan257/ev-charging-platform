-- Session Service Schema
CREATE TABLE IF NOT EXISTS charging_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    connector_id UUID NOT NULL,
    station_id UUID NOT NULL,
    rfid_card_id UUID,
    vehicle_id UUID,
    auth_method VARCHAR(30),
    ocpi_token VARCHAR(200),
    roaming_partner_id UUID,
    started_at TIMESTAMP,
    stopped_at TIMESTAMP,
    start_meter_wh BIGINT DEFAULT 0,
    stop_meter_wh BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    stop_reason VARCHAR(100),
    invoice_id UUID,
    total_amount NUMERIC(12,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON charging_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_station_id ON charging_sessions(station_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON charging_sessions(status);
