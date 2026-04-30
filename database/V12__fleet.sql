-- ============================================================
-- V12: Fleet Service Schema
-- EV Roaming Hub India
-- ============================================================

CREATE TABLE IF NOT EXISTS fleets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name        VARCHAR(200) NOT NULL,
    gst_number          VARCHAR(20) NOT NULL UNIQUE,
    contact_email       VARCHAR(200),
    contact_phone       VARCHAR(20),
    address             TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    monthly_budget_limit DECIMAL(12,2),
    current_month_spend  DECIMAL(12,2) DEFAULT 0.00,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fleet_vehicles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id        UUID NOT NULL REFERENCES fleets(id) ON DELETE CASCADE,
    registration_no VARCHAR(20) NOT NULL UNIQUE,
    make            VARCHAR(100),
    model           VARCHAR(100),
    year            INTEGER,
    battery_capacity_kwh DECIMAL(6,2),
    range_km        DECIMAL(7,2),
    vin             VARCHAR(50),         -- Vehicle Identification Number
    assigned_driver_id UUID,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fleet_drivers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id        UUID NOT NULL REFERENCES fleets(id) ON DELETE CASCADE,
    user_id         VARCHAR(100),        -- auth-service user ID
    driver_name     VARCHAR(200) NOT NULL,
    email           VARCHAR(200),
    phone           VARCHAR(20),
    employee_id     VARCHAR(50),
    rfid_card_id    VARCHAR(100),        -- RFID token for charging
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    monthly_limit   DECIMAL(10,2),       -- per-driver spending cap
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fleet_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id        UUID NOT NULL REFERENCES fleets(id),
    vehicle_id      UUID REFERENCES fleet_vehicles(id),
    driver_id       UUID REFERENCES fleet_drivers(id),
    session_id      VARCHAR(100) NOT NULL,   -- links to session-service
    station_id      VARCHAR(100),
    charge_point_id VARCHAR(100),
    started_at      TIMESTAMPTZ,
    ended_at        TIMESTAMPTZ,
    energy_kwh      DECIMAL(8,3),
    cost_inr        DECIMAL(10,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS fleet_invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fleet_id        UUID NOT NULL REFERENCES fleets(id),
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,
    total_sessions  INTEGER DEFAULT 0,
    total_energy_kwh DECIMAL(10,3) DEFAULT 0,
    subtotal_inr    DECIMAL(12,2) DEFAULT 0,
    cgst_inr        DECIMAL(10,2) DEFAULT 0,
    sgst_inr        DECIMAL(10,2) DEFAULT 0,
    igst_inr        DECIMAL(10,2) DEFAULT 0,
    total_inr       DECIMAL(12,2) DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'GENERATED',
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at         TIMESTAMPTZ
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_fleet_vehicles_fleet    ON fleet_vehicles(fleet_id);
CREATE INDEX IF NOT EXISTS idx_fleet_drivers_fleet     ON fleet_drivers(fleet_id);
CREATE INDEX IF NOT EXISTS idx_fleet_sessions_fleet    ON fleet_sessions(fleet_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_fleet_invoices_period   ON fleet_invoices(fleet_id, period_year, period_month);
