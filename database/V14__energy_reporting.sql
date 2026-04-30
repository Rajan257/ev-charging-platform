-- ============================================================
-- V14: Energy Reporting Service Schema
-- EV Roaming Hub India
-- ============================================================

CREATE TABLE IF NOT EXISTS energy_reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id          VARCHAR(100),
    report_type         VARCHAR(50) NOT NULL,  -- ENERGY, CARBON, TAX, GRID, COMPLIANCE
    period_start        TIMESTAMPTZ NOT NULL,
    period_end          TIMESTAMPTZ NOT NULL,
    total_energy_kwh    DECIMAL(12,3) DEFAULT 0,
    total_sessions      INTEGER DEFAULT 0,
    gross_revenue_inr   DECIMAL(12,2) DEFAULT 0,
    tax_inr             DECIMAL(10,2) DEFAULT 0,
    report_data         JSONB,
    generated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    generated_by        VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS carbon_credits (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    period_start            TIMESTAMPTZ NOT NULL,
    period_end              TIMESTAMPTZ NOT NULL,
    total_energy_kwh        DECIMAL(12,3) NOT NULL,
    emission_factor         DECIMAL(8,4) NOT NULL DEFAULT 0.82,  -- kg CO2/kWh (CEA 2023)
    carbon_saved_kg         DECIMAL(12,3) NOT NULL,
    carbon_saved_tonnes     DECIMAL(10,3) NOT NULL,
    trees_equivalent        INTEGER,
    petrol_litres_avoided   DECIMAL(10,2),
    calculated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tax_summaries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,
    gstin           VARCHAR(20),
    subtotal_inr    DECIMAL(12,2) DEFAULT 0,
    cgst_rate       DECIMAL(4,2) DEFAULT 9.00,
    cgst_inr        DECIMAL(10,2) DEFAULT 0,
    sgst_rate       DECIMAL(4,2) DEFAULT 9.00,
    sgst_inr        DECIMAL(10,2) DEFAULT 0,
    igst_rate       DECIMAL(4,2) DEFAULT 0.00,
    igst_inr        DECIMAL(10,2) DEFAULT 0,
    total_tax_inr   DECIMAL(10,2) DEFAULT 0,
    gross_revenue   DECIMAL(12,2) DEFAULT 0,
    filing_status   VARCHAR(20) DEFAULT 'PENDING',
    generated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(period_year, period_month, gstin)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_energy_reports_type     ON energy_reports(report_type, period_start DESC);
CREATE INDEX IF NOT EXISTS idx_energy_reports_station  ON energy_reports(station_id, period_start DESC);
CREATE INDEX IF NOT EXISTS idx_tax_summaries_period    ON tax_summaries(period_year, period_month);
