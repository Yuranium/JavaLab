DROP TABLE IF EXISTS task, category, starter_code, test_case, task_category CASCADE;

CREATE TABLE IF NOT EXISTS task
(
    id_task     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty  VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT current_timestamp,
    updated_at  TIMESTAMP    NOT NULL DEFAULT current_timestamp,
    id_author   BIGINT       NOT NULL
);

CREATE TABLE IF NOT EXISTS category
(
    id_category BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(127) NOT NULL
);

CREATE TABLE IF NOT EXISTS starter_code
(
    id_code    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code       TEXT,
    is_default BOOLEAN NOT NULL DEFAULT true,
    id_task    BIGINT UNIQUE REFERENCES task (id_task) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS test_case
(
    id_code         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    expected_output TEXT,
    is_hidden       BOOLEAN NOT NULL DEFAULT true,
    id_task         BIGINT REFERENCES task (id_task) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS task_category
(
    id_task     BIGINT REFERENCES task (id_task) ON DELETE CASCADE ON UPDATE CASCADE,
    id_category BIGINT REFERENCES category (id_category) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (id_task, id_category)
);

CREATE INDEX IF NOT EXISTS author_id_idx ON task (id_author);