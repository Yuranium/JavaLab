\c progress_service_main;

CREATE TABLE user_progress
(
    keycloak_id        UUID PRIMARY KEY,
    total_tasks_solved BIGINT      NOT NULL DEFAULT 0,
    total_attempts     BIGINT      NOT NULL DEFAULT 0,
    current_streak     INT         NOT NULL DEFAULT 0,
    longest_streak     INT         NOT NULL DEFAULT 0,
    last_login_date    DATE,
    last_activity_date DATE,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Ежедневная активность (для heat map)
CREATE TABLE daily_activity
(
    id_activity    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    keycloak_id    UUID NOT NULL,
    activity_date  DATE NOT NULL,
    tasks_solved   INT  NOT NULL DEFAULT 0,
    attempts_count INT  NOT NULL DEFAULT 0,
    login_count    INT  NOT NULL DEFAULT 0,
    CONSTRAINT uk_daily_activity_keycloak_date UNIQUE (keycloak_id, activity_date),
    CONSTRAINT fk_daily_activity_user_progress FOREIGN KEY (keycloak_id)
        REFERENCES user_progress (keycloak_id) ON DELETE CASCADE
);

-- Справочник ачивок
CREATE TABLE achievement
(
    id_achievement   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    name             VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    icon_url         VARCHAR(255),
    threshold        INT          NOT NULL,
    achievement_type VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Связь пользователь-ачивка
CREATE TABLE user_achievements
(
    id_user_achievement BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    keycloak_id         UUID    NOT NULL,
    id_achievement      BIGINT  NOT NULL,
    unlocked            BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at         TIMESTAMPTZ,
    CONSTRAINT fk_user_achievements_user_progress FOREIGN KEY (keycloak_id)
        REFERENCES user_progress (keycloak_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (id_achievement)
        REFERENCES achievement (id_achievement) ON DELETE CASCADE,
    CONSTRAINT uk_user_achievements_keycloak_achievement UNIQUE (keycloak_id, id_achievement)
);

-- Попытки решения задач
CREATE TABLE user_submissions
(
    id_submission  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    keycloak_id    UUID        NOT NULL,
    id_task        BIGINT      NOT NULL,
    attempt_number INT         NOT NULL,
    user_code      TEXT        NOT NULL,
    is_correct     BOOLEAN     NOT NULL DEFAULT FALSE,
    submitted_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_submissions_user_progress FOREIGN KEY (keycloak_id)
        REFERENCES user_progress (keycloak_id) ON DELETE CASCADE
);

-- Индексы для производительности
CREATE INDEX idx_daily_activity_date ON daily_activity (activity_date);
CREATE INDEX idx_daily_activity_keycloak_id ON daily_activity (keycloak_id);
CREATE INDEX idx_submissions_keycloak_id ON user_submissions (keycloak_id);
CREATE INDEX idx_submissions_task ON user_submissions (id_task);
CREATE INDEX idx_submissions_correct ON user_submissions (is_correct);
CREATE INDEX idx_achievements_keycloak_id ON user_achievements (keycloak_id);
CREATE INDEX idx_achievements_type ON achievement (achievement_type);
CREATE INDEX idx_user_progress_streak ON user_progress (current_streak);

-- Комментарии к таблицам
COMMENT ON TABLE user_progress IS 'Основная таблица прогресса пользователей';
COMMENT ON TABLE daily_activity IS 'Ежедневная активность для heat map графиков';
COMMENT ON TABLE achievement IS 'Справочник достижений (ачивок)';
COMMENT ON TABLE user_achievements IS 'Связь пользователей с достижениями';
COMMENT ON TABLE user_submissions IS 'История попыток решения задач';


INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES ('FIRST_BLOOD', 'Первая кровь', 'Решить первую задачу',
        'achievements/first_blood.png', 1, 'TASKS_SOLVED'),

       ('NOVICE', 'Новичок', 'Решить 5 задач',
        'achievements/novice.png', 5, 'TASKS_SOLVED'),

       ('ENTHUSIAST', 'Энтузиаст', 'Решить 10 задач',
        'achievements/enthusiast.png', 10, 'TASKS_SOLVED'),

       ('MASTER', 'Мастер', 'Решить 30 задач',
        'achievements/master.png', 30, 'TASKS_SOLVED'),

       ('LEGEND', 'Легенда', 'Решить 50 задач',
        'achievements/legend.png', 50, 'TASKS_SOLVED');

INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES ('WARM_UP', 'Разминка', '3 дня входа подряд',
        'achievements/warm_up.png', 3, 'LOGIN_STREAK'),

       ('WEEK_WARRIOR', 'Недельный воин', '7 дней входа подряд',
        'achievements/week_warrior.png', 7, 'LOGIN_STREAK'),

       ('MONTH_MASTER', 'Мастер месяца', '30 дней входа подряд',
        'achievements/month_master.png', 30, 'LOGIN_STREAK');

INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES ('CONSISTENT_3', 'Постоянный 3', '3 дня с решёнными задачами подряд',
        'achievements/consistent_3.png', 3, 'TASK_STREAK'),

       ('CONSISTENT_7', 'Постоянный 7', '7 дней с решёнными задачами подряд',
        'achievements/consistent_7.png', 7, 'TASK_STREAK'),

       ('CONSISTENT_30', 'Постоянный 30', '30 дней с решёнными задачами подряд',
        'achievements/consistent_30.png', 30, 'TASK_STREAK');
