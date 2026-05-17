CREATE TABLE customers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(150)        NOT NULL,
    cpf         VARCHAR(11)         NOT NULL UNIQUE,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);
