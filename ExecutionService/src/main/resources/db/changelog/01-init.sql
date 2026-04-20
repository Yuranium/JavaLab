-- liquibase formatted sql

-- changeset Yuranium:01-init.sql
CREATE TABLE IF NOT EXISTS test_case
(
    id_case         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    input           TEXT,
    expected_output TEXT,
    is_hidden       BOOLEAN NOT NULL DEFAULT true,
    id_task         BIGINT  NOT NULL
);