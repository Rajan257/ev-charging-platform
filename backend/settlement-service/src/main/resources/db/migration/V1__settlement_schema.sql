-- Settlement Service Schema
CREATE TABLE IF NOT EXISTS settlement_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_number VARCHAR(50) UNIQUE NOT NULL,
    cpo_network_id VARCHAR(100) NOT NULL,
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    total_sessions INTEGER DEFAULT 0,
    gross_amount NUMERIC(14,2) DEFAULT 0.00,
    platform_fee NUMERIC(14,2) DEFAULT 0.00,
    gst_on_fee NUMERIC(14,2) DEFAULT 0.00,
    net_payout NUMERIC(14,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'PENDING',
    settled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS settlement_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID REFERENCES settlement_batches(id),
    invoice_id UUID,
    session_id UUID,
    energy_kwh DOUBLE PRECISION,
    gross_amount NUMERIC(12,2),
    platform_commission_pct NUMERIC(5,2) DEFAULT 8.00,
    platform_commission NUMERIC(12,2),
    cpo_payout NUMERIC(12,2),
    currency VARCHAR(3) DEFAULT 'INR',
    created_at TIMESTAMP DEFAULT NOW()
);
