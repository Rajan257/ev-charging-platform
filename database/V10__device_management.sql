-- ============================================================
-- V10: Device Management Service Schema
-- EV Roaming Hub India
-- ============================================================

CREATE TABLE IF NOT EXISTS devices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    charge_point_id     VARCHAR(100) NOT NULL UNIQUE,
    station_id          VARCHAR(100) NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'OFFLINE',
    firmware_version    VARCHAR(50),
    manufacturer        VARCHAR(100),
    model               VARCHAR(100),
    serial_number       VARCHAR(100),
    iccid               VARCHAR(50),      -- SIM card identifier
    imsi                VARCHAR(50),      -- International Mobile Subscriber Identity
    last_heartbeat      TIMESTAMPTZ,
    last_boot_notification TIMESTAMPTZ,
    last_health_check   TIMESTAMPTZ,
    latitude            DECIMAL(10,7),
    longitude           DECIMAL(10,7),
    certificate_pem     TEXT,             -- ISO 15118 Plug & Charge certificate
    certificate_valid   BOOLEAN DEFAULT FALSE,
    certificate_expiry  TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS device_configs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    config_key      VARCHAR(200) NOT NULL,
    config_value    TEXT,
    is_readonly     BOOLEAN DEFAULT FALSE,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(device_id, config_key)
);

CREATE TABLE IF NOT EXISTS firmware_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_number  VARCHAR(50) NOT NULL,
    manufacturer    VARCHAR(100),
    model           VARCHAR(100),
    download_url    TEXT NOT NULL,
    release_notes   TEXT,
    is_stable       BOOLEAN DEFAULT FALSE,
    released_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS firmware_update_jobs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id),
    target_version  VARCHAR(50) NOT NULL,
    download_url    TEXT NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS device_health_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id),
    event_type      VARCHAR(50) NOT NULL,  -- HEARTBEAT, BOOT, FAULT, RECOVERY
    event_data      JSONB,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_devices_station_id     ON devices(station_id);
CREATE INDEX IF NOT EXISTS idx_devices_status         ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_heartbeat      ON devices(last_heartbeat);
CREATE INDEX IF NOT EXISTS idx_health_logs_device     ON device_health_logs(device_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_firmware_jobs_device   ON firmware_update_jobs(device_id, status);
