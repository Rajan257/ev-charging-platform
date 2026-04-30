-- ============================================================
-- V11: Smart Charging Service Schema
-- EV Roaming Hub India
-- ============================================================

CREATE TABLE IF NOT EXISTS charging_profiles (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id          VARCHAR(100) NOT NULL,
    charge_point_id     VARCHAR(100),
    connector_id        INTEGER DEFAULT 0,
    profile_purpose     VARCHAR(50) NOT NULL,  -- TxDefaultProfile, ChargePointMaxProfile, TxProfile
    stack_level         INTEGER NOT NULL DEFAULT 0,
    charging_rate_unit  VARCHAR(10) NOT NULL DEFAULT 'kW',  -- kW or A
    max_charging_rate   DECIMAL(10,2) NOT NULL,
    min_charging_rate   DECIMAL(10,2),
    start_schedule      TIMESTAMPTZ,
    end_schedule        TIMESTAMPTZ,
    recurrency          VARCHAR(30),    -- Daily, Weekly
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS charging_schedule_periods (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id          UUID NOT NULL REFERENCES charging_profiles(id) ON DELETE CASCADE,
    start_period_seconds INTEGER NOT NULL,  -- seconds from schedule start
    limit_kw            DECIMAL(10,2) NOT NULL,
    number_phases       INTEGER DEFAULT 3
);

CREATE TABLE IF NOT EXISTS grid_load_metrics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id      VARCHAR(100),
    measured_kw     DECIMAL(10,2) NOT NULL,
    max_capacity_kw DECIMAL(10,2) NOT NULL,
    load_percent    DECIMAL(5,2) NOT NULL,
    power_factor    DECIMAL(4,3),
    grid_status     VARCHAR(20) NOT NULL DEFAULT 'NORMAL',  -- NORMAL, HIGH, CRITICAL
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS power_limits (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id      VARCHAR(100) NOT NULL,
    limit_kw        DECIMAL(10,2) NOT NULL,
    reason          VARCHAR(200),
    set_by          VARCHAR(100),
    active_from     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active_until    TIMESTAMPTZ,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS demand_response_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type          VARCHAR(50) NOT NULL DEFAULT 'LOAD_REDUCTION',
    reduction_percent   DECIMAL(5,2) NOT NULL,
    duration_minutes    INTEGER NOT NULL,
    triggered_by        VARCHAR(100),
    reason              TEXT,
    stations_affected   INTEGER,
    triggered_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS charging_schedules (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             VARCHAR(100) NOT NULL,
    station_id          VARCHAR(100),
    preferred_start_time TIME NOT NULL,
    target_soc_percent  INTEGER DEFAULT 80,
    max_cost_inr        DECIMAL(10,2),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    session_id          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_charging_profiles_station  ON charging_profiles(station_id);
CREATE INDEX IF NOT EXISTS idx_grid_metrics_station       ON grid_load_metrics(station_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_power_limits_active        ON power_limits(station_id, is_active);
