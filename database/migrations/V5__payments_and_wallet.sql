-- ============================================================
-- V5: Payments, Wallet, Refunds
-- ============================================================

-- Payments
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id          UUID NOT NULL REFERENCES invoices(id),
    user_id             UUID NOT NULL REFERENCES users(id),

    -- Gateway details
    gateway             VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY'
                        CHECK (gateway IN ('RAZORPAY', 'PAYTM', 'PHONEPE', 'WALLET', 'MANUAL')),
    gateway_order_id    VARCHAR(255),
    gateway_payment_id  VARCHAR(255) UNIQUE,
    gateway_signature   VARCHAR(512),

    -- Payment method
    payment_method      VARCHAR(30) NOT NULL DEFAULT 'UPI'
                        CHECK (payment_method IN ('UPI', 'CARD', 'NETBANKING', 'WALLET', 'EMI', 'CASH')),
    upi_vpa             VARCHAR(100),         -- e.g. user@okaxis
    masked_account      VARCHAR(50),          -- last 4 digits

    -- Amount
    amount              DECIMAL(12, 2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    amount_paid         DECIMAL(12, 2),

    -- Status
    status              VARCHAR(20) NOT NULL DEFAULT 'INITIATED'
                        CHECK (status IN ('INITIATED', 'PENDING', 'CAPTURED', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED')),
    failure_reason      TEXT,

    initiated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_invoice_id       ON payments(invoice_id);
CREATE INDEX idx_payments_user_id          ON payments(user_id);
CREATE INDEX idx_payments_status           ON payments(status);
CREATE INDEX idx_payments_gateway_order_id ON payments(gateway_order_id);

CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Wallet (prepaid balance per user)
CREATE TABLE wallets (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID UNIQUE NOT NULL REFERENCES users(id),
    balance         DECIMAL(12, 2) NOT NULL DEFAULT 0.00
                    CHECK (balance >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    is_locked       BOOLEAN NOT NULL DEFAULT FALSE,
    locked_amount   DECIMAL(12, 2) DEFAULT 0,   -- reserved for active session
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trigger_wallets_updated_at
    BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Wallet Transactions
CREATE TABLE wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    wallet_id       UUID NOT NULL REFERENCES wallets(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    transaction_type VARCHAR(20) NOT NULL
                    CHECK (transaction_type IN ('TOPUP', 'DEBIT', 'REFUND', 'LOCK', 'UNLOCK', 'ADJUSTMENT')),
    amount          DECIMAL(12, 2) NOT NULL,
    balance_before  DECIMAL(12, 2) NOT NULL,
    balance_after   DECIMAL(12, 2) NOT NULL,
    reference_id    UUID,                       -- invoice or payment ID
    description     TEXT,
    payment_id      UUID REFERENCES payments(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wallet_tx_wallet_id ON wallet_transactions(wallet_id);
CREATE INDEX idx_wallet_tx_user_id   ON wallet_transactions(user_id);
CREATE INDEX idx_wallet_tx_created   ON wallet_transactions(created_at DESC);

-- Refunds
CREATE TABLE refunds (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payment_id          UUID NOT NULL REFERENCES payments(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    gateway_refund_id   VARCHAR(255) UNIQUE,
    amount              DECIMAL(12, 2) NOT NULL,
    reason              TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'INITIATED'
                        CHECK (status IN ('INITIATED', 'PROCESSING', 'PROCESSED', 'FAILED')),
    initiated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_user_id    ON refunds(user_id);
