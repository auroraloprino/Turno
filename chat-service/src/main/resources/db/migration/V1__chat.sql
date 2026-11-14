CREATE TABLE
    conversazioni (
        id BIGSERIAL PRIMARY KEY,
        tipo VARCHAR(10) NOT NULL,
        nome VARCHAR(100) NOT NULL
    );

CREATE TABLE
    conversazione_partecipanti (
        conversazione_id BIGINT NOT NULL REFERENCES conversazioni (id),
        user_id BIGINT NOT NULL,
        PRIMARY KEY (conversazione_id, user_id)
    );

CREATE TABLE
    messaggi (
        id BIGSERIAL PRIMARY KEY,
        conversazione_id BIGINT NOT NULL REFERENCES conversazioni (id),
        sender_id BIGINT NOT NULL,
        sender_name VARCHAR(100) NOT NULL,
        testo TEXT NOT NULL,
        allegato_url VARCHAR(500),
        allegato_nome VARCHAR(255),
        timestamp TIMESTAMPTZ NOT NULL DEFAULT now (),
        modificato BOOLEAN NOT NULL DEFAULT false,
        eliminato BOOLEAN NOT NULL DEFAULT false
    );