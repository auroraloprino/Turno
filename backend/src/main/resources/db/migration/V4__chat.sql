CREATE TABLE conversazioni (
    id      BIGSERIAL PRIMARY KEY,
    tipo    VARCHAR(10)  NOT NULL,
    nome    VARCHAR(100) NOT NULL
);

CREATE TABLE conversazione_partecipanti (
    conversazione_id BIGINT NOT NULL REFERENCES conversazioni(id),
    user_id          BIGINT NOT NULL REFERENCES users(id),
    PRIMARY KEY (conversazione_id, user_id)
);

CREATE TABLE messaggi (
    id               BIGSERIAL PRIMARY KEY,
    conversazione_id BIGINT      NOT NULL REFERENCES conversazioni(id),
    sender_id        BIGINT      NOT NULL REFERENCES users(id),
    testo            TEXT        NOT NULL,
    timestamp        TIMESTAMPTZ NOT NULL DEFAULT now()
);
