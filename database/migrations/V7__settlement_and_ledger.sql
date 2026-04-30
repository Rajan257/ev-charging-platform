-- ============================================================
-- V7: Settlement, Revenue Distribution, Ledger
-- ============================================================

-- CPO Contracts (revenue share agreements)
CREATE TABLE cpo_contracts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cpo_network_id      UUID NOT NULL REFERENCES cpo_networks(id),
    revenue_share_pct   DECIMAL(5, 2) NOT NULL DEFAULT 85.00,  -- % to CPO
    platform_fee_pct    DECIMAL(5, 2) NOT NULL DEFAULT 10.00,  -- % platform fee
    roaming_fee_pct     DECIMAL(5, 2) NOT NULL DEFAULT 5.00,   -- % roaming margin
    bank_account_name   VARCHAR(255),
    bank_account_number VARCHAR(100),
    bank_ifsc           VARCHAR(20),
    upi_vpa             VARCHAR(100),
    settlement_cycle    VARCHAR(20) DEFAULT 'WEEKLY'
                        CHECK (settlement_cycle IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    valid_from          DATE NOT NULL,
    valid_to            DATE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Settlement Runs
CREATE TABLE settlement_records (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    settlement_number   VARCHAR(50) UNIQUE NOT NULL,  -- SETL-2024-W01-TATA
    cpo_network_id      UUID NOT NULL REFERENCES cpo_networks(id),
    contract_id         UUID NOT NULL REFERENCES cpo_contracts(id),

    -- Period
    period_start        DATE NOT NULL,
    period_end          DATE NOT NULL,

    -- Sessions covered
    session_count       INT NOT NULL DEFAULT 0,
    total_energy_kwh    DECIMAL(12, 4) DEFAULT 0,
    gross_revenue       DECIMAL(14, 2) NOT NULL DEFAULT 0,

    -- Deductions
    platform_fee        DECIMAL(12, 2) NOT NULL DEFAULT 0,
    roaming_charges     DECIMAL(12, 2) DEFAULT 0,
    gst_deduction       DECIMAL(12, 2) DEFAULT 0,

    -- Payout
    net_payout          DECIMAL(14, 2) NOT NULL DEFAULT 0,
    currency            VARCHAR(3) DEFAULT 'INR',

    -- Status
    status              VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'PAID', 'DISPUTED', 'CANCELLED')),
    approved_by         UUID REFERENCES users(id),
    approved_at         TIMESTAMPTZ,
    paid_at             TIMESTAMPTZ,
    payment_reference   VARCHAR(255),
    notes               TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_settlements_cpo_id ON settlement_records(cpo_network_id);
CREATE INDEX idx_settlements_status ON settlement_records(status);
CREATE INDEX idx_settlements_period ON settlement_records(period_start, period_end);

CREATE TRIGGER trigger_settlements_updated_at
    BEFORE UPDATE ON settlement_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Settlement Line Items (per session)
CREATE TABLE settlement_line_items (
    id                  BIGSERIAL PRIMARY KEY,
    settlement_id       UUID NOT NULL REFERENCES settlement_records(id) ON DELETE CASCADE,
    session_id          UUID NOT NULL REFERENCES charging_sessions(id),
    invoice_id          UUID REFERENCES invoices(id),
    energy_kwh          DECIMAL(10, 4) NOT NULL,
    gross_amount        DECIMAL(12, 2) NOT NULL,
    platform_fee        DECIMAL(12, 2) NOT NULL,
    net_amount          DECIMAL(12, 2) NOT NULL,
    is_roaming          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_settlement_lines_settlement ON settlement_line_items(settlement_id);

-- Settlement Ledger (accounting entries)
CREATE TABLE settlement_ledger (
    id                  BIGSERIAL PRIMARY KEY,
    settlement_id       UUID NOT NULL REFERENCES settlement_records(id),
    cpo_network_id      UUID NOT NULL REFERENCES cpo_networks(id),
    entry_type          VARCHAR(30) NOT NULL
                        CHECK (entry_type IN ('CREDIT', 'DEBIT', 'FEE', 'ROAMING', 'TAX', 'ADJUSTMENT')),
    amount              DECIMAL(12, 2) NOT NULL,
    description         TEXT,
    reference_id        VARCHAR(255),
    balance_after       DECIMAL(14, 2),
    entry_date          DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_cpo_id     ON settlement_ledger(cpo_network_id);
CREATE INDEX idx_ledger_entry_date ON settlement_ledger(entry_date DESC);

-- Reconciliation Reports
CREATE TABLE reconciliation_reports (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_date         DATE NOT NULL,
    period_start        DATE NOT NULL,
    period_end          DATE NOT NULL,
    total_invoiced      DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_collected     DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_settled       DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_outstanding   DECIMAL(14, 2) NOT NULL DEFAULT 0,
    discrepancy_amount  DECIMAL(14, 2) DEFAULT 0,
    notes               TEXT,
    generated_by        UUID REFERENCES users(id),
    status              VARCHAR(20) DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'REVIEWED', 'APPROVED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
