CREATE TABLE transactions (
  id BIGSERIAL PRIMARY KEY,
  amount NUMERIC NOT NULL,
  type VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  date TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);
