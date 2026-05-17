CREATE TABLE invoices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id         UUID            NOT NULL REFERENCES cards(id),
    reference_month VARCHAR(7)      NOT NULL,  -- formato: 2025-05
    total_amount    NUMERIC(15,2)   NOT NULL DEFAULT 0,
    paid_amount     NUMERIC(15,2)   NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN',  -- OPEN, PARTIALLY_PAID, PAID
    due_date        DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE(card_id, reference_month)
);

CREATE TABLE invoice_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id      UUID            NOT NULL REFERENCES invoices(id),
    transaction_id  UUID            NOT NULL REFERENCES card_transactions(id),
    amount          NUMERIC(15,2)   NOT NULL,
    description     VARCHAR(255),
    transaction_date TIMESTAMP      NOT NULL
);
