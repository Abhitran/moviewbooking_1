-- Payment Service Database Schema

CREATE TABLE IF NOT EXISTS payments (
    payment_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id              UUID NOT NULL,
    user_id                 UUID NOT NULL,
    amount                  DECIMAL(10,2) NOT NULL,
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    gateway_transaction_id  VARCHAR(255),
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_payments_user ON payments(user_id);

CREATE TABLE IF NOT EXISTS refunds (
    refund_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id  UUID NOT NULL REFERENCES payments(payment_id),
    amount      DECIMAL(10,2) NOT NULL,
    status      VARCHAR(50) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refunds_payment ON refunds(payment_id);
