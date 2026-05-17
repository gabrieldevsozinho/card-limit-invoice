CREATE TABLE card_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id         UUID            NOT NULL REFERENCES cards(id),
    amount          NUMERIC(15,2)   NOT NULL,
    type            VARCHAR(20)     NOT NULL,  -- PURCHASE, REVERSAL
    description     VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_card_transactions_card_id ON card_transactions(card_id);
CREATE INDEX idx_card_transactions_created_at ON card_transactions(created_at);
