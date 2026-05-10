-- Payment Service Schema
CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    balance NUMERIC(12,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID,
    user_id UUID NOT NULL,
    gateway VARCHAR(30),
    payment_method VARCHAR(30),
    status VARCHAR(30) DEFAULT 'INITIATED',
    gateway_order_id VARCHAR(100),
    gateway_payment_id VARCHAR(100),
    gateway_signature VARCHAR(500),
    upi_vpa VARCHAR(100),
    amount NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2),
    currency VARCHAR(3) DEFAULT 'INR',
    failure_reason TEXT,
    initiated_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID REFERENCES wallets(id),
    user_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    balance_before NUMERIC(12,2),
    balance_after NUMERIC(12,2),
    reference_id UUID,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
