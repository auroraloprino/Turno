CREATE TABLE
    audit_log (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        tipo VARCHAR(10) NOT NULL,
        timestamp TIMESTAMPTZ NOT NULL
    );