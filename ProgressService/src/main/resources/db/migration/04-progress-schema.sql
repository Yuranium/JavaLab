-- ============================================
-- 04-progress-schema.sql
-- Progress Service Database Schema
-- ============================================

-- Основная таблица прогресса пользователя
CREATE TABLE user_progress (
    username            VARCHAR(50) PRIMARY KEY,
    total_tasks_solved  BIGINT NOT NULL DEFAULT 0,
    total_attempts      BIGINT NOT NULL DEFAULT 0,
    current_streak      INT NOT NULL DEFAULT 0,
    longest_streak      INT NOT NULL DEFAULT 0,
    last_login_date     DATE,
    last_activity_date  DATE,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Ежедневная активность (для heat map)
CREATE TABLE daily_activity (
    id_activity      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username         VARCHAR(50) NOT NULL,
    activity_date    DATE NOT NULL,
    tasks_solved     INT NOT NULL DEFAULT 0,
    attempts_count   INT NOT NULL DEFAULT 0,
    login_count      INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_daily_activity_username_date UNIQUE (username, activity_date),
    CONSTRAINT fk_daily_activity_user_progress FOREIGN KEY (username) 
        REFERENCES user_progress(username) ON DELETE CASCADE
);

-- Справочник ачивок
CREATE TABLE achievement (
    id_achievement   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    code             VARCHAR(50) NOT NULL UNIQUE,
    name             VARCHAR(100) NOT NULL,
    description      TEXT NOT NULL,
    icon_url         VARCHAR(255),
    threshold        INT NOT NULL,
    achievement_type VARCHAR(20) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Связь пользователь-ачивка
CREATE TABLE user_achievements (
    id_user_achievement  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username             VARCHAR(50) NOT NULL,
    id_achievement       BIGINT NOT NULL,
    unlocked             BOOLEAN NOT NULL DEFAULT FALSE,
    unlocked_at          TIMESTAMP,
    CONSTRAINT fk_user_achievements_user_progress FOREIGN KEY (username) 
        REFERENCES user_progress(username) ON DELETE CASCADE,
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (id_achievement) 
        REFERENCES achievement(id_achievement) ON DELETE CASCADE,
    CONSTRAINT uk_user_achievements_username_achievement UNIQUE (username, id_achievement)
);

-- Попытки решения задач
CREATE TABLE user_submissions (
    id_submission    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username         VARCHAR(50) NOT NULL,
    id_task          BIGINT NOT NULL,
    attempt_number   INT NOT NULL,
    user_code        TEXT NOT NULL,
    is_correct       BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_submissions_user_progress FOREIGN KEY (username) 
        REFERENCES user_progress(username) ON DELETE CASCADE
);

-- Индексы для производительности
CREATE INDEX idx_daily_activity_date ON daily_activity(activity_date);
CREATE INDEX idx_daily_activity_username ON daily_activity(username);
CREATE INDEX idx_submissions_username ON user_submissions(username);
CREATE INDEX idx_submissions_task ON user_submissions(id_task);
CREATE INDEX idx_submissions_correct ON user_submissions(is_correct);
CREATE INDEX idx_achievements_username ON user_achievements(username);
CREATE INDEX idx_achievements_type ON achievement(achievement_type);
CREATE INDEX idx_user_progress_streak ON user_progress(current_streak);

-- Комментарии к таблицам
COMMENT ON TABLE user_progress IS 'Основная таблица прогресса пользователей';
COMMENT ON TABLE daily_activity IS 'Ежедневная активность для heat map графиков';
COMMENT ON TABLE achievement IS 'Справочник достижений (ачивок)';
COMMENT ON TABLE user_achievements IS 'Связь пользователей с достижениями';
COMMENT ON TABLE user_submissions IS 'История попыток решения задач';
