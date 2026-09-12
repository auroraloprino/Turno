CREATE TABLE turni (
    id BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES users(id),
    data DATE NOT NULL,
    inizio TIME NOT NULL,
    fine TIME NOT NULL
);
