CREATE TABLE cards (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id     UUID            NOT NULL REFERENCES customers(id),
    card_number     VARCHAR(16)     NOT NULL UNIQUE,
    credit_limit    NUMERIC(15,2)   NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);
