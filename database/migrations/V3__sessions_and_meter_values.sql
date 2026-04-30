-- ============================================================
-- V3: Charging Sessions, Meter Values, Session Logs
-- ============================================================

-- Charging Sessions
CREATE TABLE charging_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id      VARCHAR(100) UNIQUE NOT NULL,     -- OCPP transaction ID
    user_id             UUID NOT NULL REFERENCES users(id),
    connector_id        UUID NOT NULL REFERENCES connectors(id),
    station_id          UUID NOT NULL REFERENCES charging_stations(id),
    rfid_card_id        UUID REFERENCES rfid_cards(id),
    vehicle_id          UUID REFERENCES vehicles(id),

    -- Auth method used
    auth_method         VARCHAR(30) NOT NULL DEFAULT 'APP'
                        CHECK (auth_method IN ('RFID', 'APP', 'PLUG_AND_CHARGE', 'REMOTE', 'ROAMING')),
    -- OCPI token for roaming sessions
    ocpi_token          VARCHAR(255),
    roaming_partner_id  UUID,                             -- FK added in V6

    -- Timing
    started_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    stopped_at          TIMESTAMPTZ,
    duration_seconds    INT GENERATED ALWAYS AS (
                            EXTRACT(EPOCH FROM (stopped_at - started_at))::INT
                        ) STORED,

    -- Energy
    start_meter_wh      BIGINT NOT NULL DEFAULT 0,        -- Wh at session start
    stop_meter_wh       BIGINT,                           -- Wh at session stop
    energy_kwh          DECIMAL(10, 4) GENERATED ALWAYS AS (
                            CASE WHEN stop_meter_wh IS NOT NULL
                            THEN (stop_meter_wh - start_meter_wh) / 1000.0
                            ELSE NULL END
                        ) STORED,

    -- Status
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'COMPLETED', 'STOPPED_BY_USER', 'STOPPED_BY_EV',
                                          'STOPPED_BY_OPERATOR', 'FAULTED', 'EXPIRED', 'INVALID')),
    stop_reason         VARCHAR(100),

    -- Billing
    invoice_id          UUID,                             -- FK added in V4
    total_amount        DECIMAL(12, 2),

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sessions_user_id      ON charging_sessions(user_id);
CREATE INDEX idx_sessions_connector_id ON charging_sessions(connector_id);
CREATE INDEX idx_sessions_station_id   ON charging_sessions(station_id);
CREATE INDEX idx_sessions_status       ON charging_sessions(status);
CREATE INDEX idx_sessions_started_at   ON charging_sessions(started_at DESC);
CREATE INDEX idx_sessions_transaction  ON charging_sessions(transaction_id);

CREATE TRIGGER trigger_sessions_updated_at
    BEFORE UPDATE ON charging_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Meter Values (sampled energy readings during session)
CREATE TABLE meter_values (
    id              BIGSERIAL PRIMARY KEY,
    session_id      UUID NOT NULL REFERENCES charging_sessions(id) ON DELETE CASCADE,
    connector_id    UUID NOT NULL REFERENCES connectors(id),
    timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Measurands (OCPP 2.0.1 Measurand enum)
    energy_wh       BIGINT,                  -- Energy.Active.Import.Register (Wh)
    power_w         INT,                     -- Power.Active.Import (W)
    voltage_v       DECIMAL(8, 2),           -- Voltage (V)
    current_a       DECIMAL(8, 2),           -- Current.Import (A)
    soc_percent     SMALLINT,                -- SoC.Battery (%)
    temperature_c   DECIMAL(5, 2),           -- Temperature (°C)

    context         VARCHAR(50) DEFAULT 'Sample.Periodic',
    raw_payload     JSONB                    -- full OCPP MeterValue object
);

CREATE INDEX idx_meter_values_session_id  ON meter_values(session_id);
CREATE INDEX idx_meter_values_timestamp   ON meter_values(timestamp DESC);

-- Session Events Log (OCPP message history)
CREATE TABLE session_logs (
    id              BIGSERIAL PRIMARY KEY,
    session_id      UUID REFERENCES charging_sessions(id),
    charge_point_id VARCHAR(100),
    message_type    VARCHAR(50) NOT NULL,    -- e.g. TransactionEvent, StatusNotification
    message_id      VARCHAR(100),
    direction       VARCHAR(10) NOT NULL DEFAULT 'INBOUND'
                    CHECK (direction IN ('INBOUND', 'OUTBOUND')),
    payload         JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_session_logs_session_id ON session_logs(session_id);
CREATE INDEX idx_session_logs_created_at ON session_logs(created_at DESC);

-- OCPP Heartbeat tracking
CREATE TABLE ocpp_heartbeats (
    id              BIGSERIAL PRIMARY KEY,
    charge_point_id VARCHAR(100) NOT NULL,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    interval_seconds INT
);

CREATE INDEX idx_heartbeats_cp_id ON ocpp_heartbeats(charge_point_id);
