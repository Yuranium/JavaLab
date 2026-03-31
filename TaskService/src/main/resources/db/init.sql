CREATE DATABASE task_service_main;
\c task_service_main;

DROP TABLE IF EXISTS task, category, starter_code, test_case, task_category CASCADE;

CREATE TABLE IF NOT EXISTS task
(
    id_task     BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty  VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    id_author   UUID         NOT NULL
);

CREATE TABLE IF NOT EXISTS category
(
    id_category BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    title       VARCHAR(127) UNIQUE NOT NULL,
    description TEXT                NOT NULL,
    created_at  TIMESTAMPTZ         NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS starter_code
(
    id_code    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code       TEXT,
    is_default BOOLEAN DEFAULT true,
    id_task    BIGINT UNIQUE REFERENCES task (id_task) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS test_case
(
    id_case         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    input           TEXT,
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

INSERT INTO category(title, description)
VALUES ('JAVA_CORE', 'Основы языка, ООП, синтаксис, примитивные типы, исключения'),
       ('JAVA_GENERICS', 'Обобщённые типы, вайлд-карды, стирание типов, ограничения типовй'),
       ('JAVA_COLLECTIONS', 'Работа со структурами данных: List, Set, Map, их реализациями'),
       ('JAVA_LAMBDAS', 'Лямбда-выражения, функциональные интерфейсы, ссылки на методы и конструкторы'),
       ('JAVA_STREAM_API', 'Функциональная обработка коллекций: filter, map, reduce, лямбда-выражения');

CREATE OR REPLACE FUNCTION set_default_starter_code()
    RETURNS TRIGGER AS
$$
BEGIN
    IF NEW.is_default OR NEW.is_default IS NULL THEN
        NEW.is_default = true;
        NEW.code :=
                'public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }';
    END IF;
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER check_starter_code
    BEFORE INSERT
    ON starter_code
    FOR EACH ROW
EXECUTE FUNCTION set_default_starter_code();

CREATE OR REPLACE FUNCTION check_starter_code()
    RETURNS TRIGGER AS
$$
DECLARE
    starter_code TEXT :=
        'public class Main {
            public static void main(String[] args) {
                System.out.println("Hello, World!");
            }
        }';
BEGIN
    IF NEW.code = starter_code THEN
        NEW.is_default = true;
    ELSE
        NEW.is_default = false;
    END IF;
    RETURN NEW;
END;
$$
    LANGUAGE plpgsql;

CREATE TRIGGER check_starter_code_1
    BEFORE UPDATE
    ON starter_code
    FOR EACH ROW
EXECUTE FUNCTION check_starter_code();