-- ============================================
-- 05-progress-data.sql
-- Initial data for achievement dictionary
-- ============================================

-- Ачивки за решённые задачи (TASKS_SOLVED)
INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES 
    ('FIRST_BLOOD', 'Первая кровь', 'Решить первую задачу', 
     'http://localhost:9000/java-lab/achievements/first_blood.png', 1, 'TASKS_SOLVED'),
    
    ('NOVICE', 'Новичок', 'Решить 10 задач', 
     'http://localhost:9000/java-lab/achievements/novice.png', 10, 'TASKS_SOLVED'),
    
    ('ENTHUSIAST', 'Энтузиаст', 'Решить 50 задач', 
     'http://localhost:9000/java-lab/achievements/enthusiast.png', 50, 'TASKS_SOLVED'),
    
    ('MASTER', 'Мастер', 'Решить 100 задач', 
     'http://localhost:9000/java-lab/achievements/master.png', 100, 'TASKS_SOLVED'),
    
    ('LEGEND', 'Легенда', 'Решить 500 задач', 
     'http://localhost:9000/java-lab/achievements/legend.png', 500, 'TASKS_SOLVED');

-- Ачивки за streak входов (LOGIN_STREAK)
INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES 
    ('WARM_UP', 'Разминка', '3 дня входа подряд', 
     'http://localhost:9000/java-lab/achievements/warm_up.png', 3, 'LOGIN_STREAK'),
    
    ('WEEK_WARRIOR', 'Недельный воин', '7 дней входа подряд', 
     'http://localhost:9000/java-lab/achievements/week_warrior.png', 7, 'LOGIN_STREAK'),
    
    ('MONTH_MASTER', 'Мастер месяца', '30 дней входа подряд', 
     'http://localhost:9000/java-lab/achievements/month_master.png', 30, 'LOGIN_STREAK');

-- Ачивки за streak задач (TASK_STREAK)
INSERT INTO achievement (code, name, description, icon_url, threshold, achievement_type)
VALUES 
    ('CONSISTENT_3', 'Постоянный 3', '3 дня с решёнными задачами подряд', 
     'http://localhost:9000/java-lab/achievements/consistent_3.png', 3, 'TASK_STREAK'),
    
    ('CONSISTENT_7', 'Постоянный 7', '7 дней с решёнными задачами подряд', 
     'http://localhost:9000/java-lab/achievements/consistent_7.png', 7, 'TASK_STREAK'),
    
    ('CONSISTENT_30', 'Постоянный 30', '30 дней с решёнными задачами подряд', 
     'http://localhost:9000/java-lab/achievements/consistent_30.png', 30, 'TASK_STREAK');
