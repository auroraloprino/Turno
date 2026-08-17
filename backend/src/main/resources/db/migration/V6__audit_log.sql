CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tipo VARCHAR(10) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);
