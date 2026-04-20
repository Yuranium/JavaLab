CREATE TABLE IF NOT EXISTS execution_code_state
(
    id_request  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_task     BIGINT NOT NULL,
    username    TEXT   NOT NULL,
    id_session  UUID   NOT NULL,
    source_code TEXT   NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE (id_task, username)
);