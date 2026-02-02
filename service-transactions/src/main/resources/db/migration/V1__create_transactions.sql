-- Sequence (idempotent)
CREATE SEQUENCE IF NOT EXISTS public.transactions_seq;

-- Table (idempotent)
CREATE TABLE IF NOT EXISTS public.transactions (
  id           BIGINT PRIMARY KEY DEFAULT nextval('public.transactions_seq'),
  amount       NUMERIC NOT NULL,
  type         VARCHAR(255) NOT NULL,
  description  VARCHAR(255),
  date         TIMESTAMP NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT now(),
  updated_at   TIMESTAMP DEFAULT now(),
  deleted_at   TIMESTAMP
);

-- Ownership
ALTER SEQUENCE public.transactions_seq
  OWNED BY public.transactions.id;
