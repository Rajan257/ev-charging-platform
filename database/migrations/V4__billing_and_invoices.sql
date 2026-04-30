-- ============================================================
-- V4: Billing, Tariffs, Invoices, Tax
-- ============================================================

-- Tariffs
CREATE TABLE tariffs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cpo_network_id  UUID NOT NULL REFERENCES cpo_networks(id),
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',

    -- Pricing components
    price_per_kwh   DECIMAL(10, 4) NOT NULL,     -- per kWh rate
    price_per_min   DECIMAL(10, 4) DEFAULT 0,    -- idle/parking fee per minute
    flat_fee        DECIMAL(10, 2) DEFAULT 0,    -- session start fee
    min_price       DECIMAL(10, 2) DEFAULT 0,    -- minimum charge per session

    -- Tax
    gst_rate        DECIMAL(5, 2) NOT NULL DEFAULT 18.00,  -- GST %
    gst_inclusive   BOOLEAN NOT NULL DEFAULT FALSE,

    -- Dynamic pricing
    is_dynamic      BOOLEAN NOT NULL DEFAULT FALSE,
    peak_multiplier DECIMAL(4, 2) DEFAULT 1.0,
    off_peak_rate   DECIMAL(10, 4),
    peak_hours      JSONB,                       -- {"start": "18:00", "end": "22:00"}

    -- Validity
    valid_from      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_to        TIMESTAMPTZ,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tariffs_cpo_id ON tariffs(cpo_network_id);

CREATE TRIGGER trigger_tariffs_updated_at
    BEFORE UPDATE ON tariffs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Backfill connector FK to tariff
ALTER TABLE connectors ADD CONSTRAINT fk_connector_tariff
    FOREIGN KEY (tariff_id) REFERENCES tariffs(id);

-- Invoices
CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_number      VARCHAR(50) UNIQUE NOT NULL,   -- INV-2024-00001
    session_id          UUID NOT NULL REFERENCES charging_sessions(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    cpo_network_id      UUID NOT NULL REFERENCES cpo_networks(id),

    -- Billing period
    billing_start       TIMESTAMPTZ NOT NULL,
    billing_end         TIMESTAMPTZ NOT NULL,

    -- Energy
    energy_kwh          DECIMAL(10, 4) NOT NULL,

    -- Charges
    energy_charge       DECIMAL(12, 2) NOT NULL,
    idle_charge         DECIMAL(12, 2) DEFAULT 0,
    flat_charge         DECIMAL(12, 2) DEFAULT 0,
    roaming_surcharge   DECIMAL(12, 2) DEFAULT 0,
    subtotal            DECIMAL(12, 2) NOT NULL,

    -- Tax breakdown (India GST)
    cgst_amount         DECIMAL(12, 2) DEFAULT 0,
    sgst_amount         DECIMAL(12, 2) DEFAULT 0,
    igst_amount         DECIMAL(12, 2) DEFAULT 0,
    total_tax           DECIMAL(12, 2) NOT NULL DEFAULT 0,

    -- Total
    total_amount        DECIMAL(12, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',

    -- Status
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'WAIVED', 'DISPUTED', 'REFUNDED')),
    due_date            TIMESTAMPTZ,
    paid_at             TIMESTAMPTZ,

    -- PDF
    pdf_url             TEXT,
    notes               TEXT,

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_session_id ON invoices(session_id);
CREATE INDEX idx_invoices_user_id    ON invoices(user_id);
CREATE INDEX idx_invoices_status     ON invoices(status);
CREATE INDEX idx_invoices_created_at ON invoices(created_at DESC);

CREATE TRIGGER trigger_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Invoice Line Items
CREATE TABLE invoice_line_items (
    id          BIGSERIAL PRIMARY KEY,
    invoice_id  UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(255) NOT NULL,
    quantity    DECIMAL(10, 4) NOT NULL,
    unit        VARCHAR(20) NOT NULL,   -- kWh, min, session
    unit_price  DECIMAL(12, 4) NOT NULL,
    amount      DECIMAL(12, 2) NOT NULL,
    line_type   VARCHAR(30) NOT NULL
                CHECK (line_type IN ('ENERGY', 'IDLE', 'FLAT_FEE', 'ROAMING', 'TAX', 'DISCOUNT'))
);

CREATE INDEX idx_line_items_invoice_id ON invoice_line_items(invoice_id);

-- Backfill session invoice FK
ALTER TABLE charging_sessions ADD CONSTRAINT fk_session_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoices(id);

-- Backfill session total
ALTER TABLE charging_sessions ADD CONSTRAINT check_total_non_negative
    CHECK (total_amount >= 0);
