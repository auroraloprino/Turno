CREATE TABLE permessi (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tipo VARCHAR(20)  NOT NULL,
    dal DATE NOT NULL,
    al DATE NOT NULL,
    ora_inizio TIME,
    ora_fine TIME,
    note  TEXT,
    stato VARCHAR(20) NOT NULL DEFAULT 'IN_ATTESA'
);
