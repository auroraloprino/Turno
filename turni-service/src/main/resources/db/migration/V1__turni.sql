CREATE TABLE
    turni (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        data DATE NOT NULL,
        inizio TIME NOT NULL,
        fine TIME NOT NULL
    );