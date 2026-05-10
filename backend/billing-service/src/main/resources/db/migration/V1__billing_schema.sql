-- Billing Service Schema
CREATE TABLE IF NOT EXISTS tariffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    network_id VARCHAR(100),
    price_per_kwh NUMERIC(10,4) NOT NULL DEFAULT 8.00,
    price_per_min NUMERIC(10,4) NOT NULL DEFAULT 0.00,
    flat_fee NUMERIC(10,2) DEFAULT 0.00,
    min_price NUMERIC(10,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    session_id UUID,
    user_id UUID NOT NULL,
    cpo_network_id VARCHAR(100),
    billing_start TIMESTAMP,
    billing_end TIMESTAMP,
    energy_kwh DOUBLE PRECISION,
    energy_charge NUMERIC(10,2),
    idle_charge NUMERIC(10,2),
    flat_charge NUMERIC(10,2),
    subtotal NUMERIC(10,2),
    cgst_amount NUMERIC(10,2),
    sgst_amount NUMERIC(10,2),
    igst_amount NUMERIC(10,2),
    total_tax NUMERIC(10,2),
    total_amount NUMERIC(10,2),
    currency VARCHAR(3) DEFAULT 'INR',
    status VARCHAR(20) DEFAULT 'PENDING',
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS invoice_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(200),
    quantity DOUBLE PRECISION,
    unit VARCHAR(20),
    unit_price NUMERIC(10,4),
    amount NUMERIC(10,2),
    line_type VARCHAR(30)
);

-- Default tariff
INSERT INTO tariffs (id, name, network_id, price_per_kwh, price_per_min, flat_fee, min_price, currency)
VALUES ('00000000-0001-0001-0001-000000000001', 'Standard Tariff', 'EV-ROAMING-HUB', 8.00, 0.00, 25.00, 50.00, 'INR')
ON CONFLICT DO NOTHING;
