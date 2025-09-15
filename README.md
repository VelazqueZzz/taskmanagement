Не большой проект распределения задач в отделе или компании. Стэк HTML+CSS+SpringBoot+MySql
незабудьте в application.properties выставить свои настройки
Скрипт для создания базы и таблиц:


-- Создание базы данных
CREATE DATABASE IF NOT EXISTS task_management_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE task_management_db;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB;

-- Таблица задач
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    due_date DATE,
    created_at DATE,
    user_id BIGINT NOT NULL,
    created_at_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_due_date (due_date),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- Таблица уведомлений
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    type ENUM('INFO', 'WARNING', 'SUCCESS', 'ERROR', 'ASSIGNMENT', 'REMINDER') NOT NULL DEFAULT 'INFO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Таблица комментариев к задачам
CREATE TABLE IF NOT EXISTS task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB;

-- Таблица истории изменений задач
CREATE TABLE IF NOT EXISTS task_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB;

-- Вставка пользователей с ОБЫЧНЫМИ текстовыми паролями
INSERT INTO users (username, password, email, full_name, role) VALUES
('admin', 'admin123', 'admin@company.com', 'Администратор Системы', 'ADMIN'),
('user1', 'user123', 'user1@company.com', 'Тестовый Пользователь', 'USER'),
('user2', 'password', 'user2@company.com', 'Второй Пользователь', 'USER');

-- Вставка тестовых задач
INSERT INTO tasks (title, description, status, priority, due_date, created_at, user_id) VALUES
('Настроить сервер', 'Настроить production сервер для нового проекта', 'PENDING', 'HIGH', DATE_ADD(CURDATE(), INTERVAL 7 DAY), CURDATE(), 1),
('Разработать API', 'Создать REST API для модуля пользователей', 'IN_PROGRESS', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 14 DAY), CURDATE(), 2),
('Написать документацию', 'Подготовить техническую документацию проекта', 'PENDING', 'LOW', DATE_ADD(CURDATE(), INTERVAL 30 DAY), CURDATE(), 1),
('Тестирование системы', 'Провести полное тестирование всех модулей', 'PENDING', 'URGENT', DATE_ADD(CURDATE(), INTERVAL 3 DAY), CURDATE(), 3),
('Дизайн интерфейса', 'Создать макеты пользовательского интерфейса', 'COMPLETED', 'MEDIUM', DATE_ADD(CURDATE(), INTERVAL 5 DAY), CURDATE(), 2);

-- Вставка тестовых уведомлений
INSERT INTO notifications (message, is_read, type, user_id) VALUES
('Добро пожаловать в систему управления задачами!', FALSE, 'INFO', 1),
('Вам назначена новая задача: "Настроить сервер"', FALSE, 'ASSIGNMENT', 1),
('Срок выполнения задачи "Разработать API" приближается', FALSE, 'REMINDER', 2),
('Задача "Дизайн интерфейса" выполнена', TRUE, 'SUCCESS', 2),
('Новый комментарий к вашей задаче', FALSE, 'INFO', 3);

-- Вставка тестовых комментариев
INSERT INTO task_comments (content, task_id, user_id) VALUES
('Начал работу над задачей', 2, 2),
('Нужно уточнить требования к API', 2, 1),
('Сервер готов к настройке', 1, 1),
('Дизайн утвержден заказчиком', 5, 2),
('Найдены баги в тестировании', 4, 3);

-- Вставка тестовой истории изменений
INSERT INTO task_history (task_id, field_name, old_value, new_value, changed_by) VALUES
(2, 'status', 'PENDING', 'IN_PROGRESS', 2),
(5, 'status', 'IN_PROGRESS', 'COMPLETED', 2),
(4, 'priority', 'HIGH', 'URGENT', 1);

-- Показать информацию о созданных таблицах
SELECT 
    TABLE_NAME AS 'Таблица',
    TABLE_ROWS AS 'Количество записей',
    DATA_LENGTH AS 'Размер данных (байт)',
    CREATE_TIME AS 'Время создания'
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'task_management_db'
ORDER BY TABLE_NAME;

-- Показать структуру таблиц
SHOW TABLES;

-- Проверка данных пользователей
SELECT '=== ПОЛЬЗОВАТЕЛИ ===' AS '';
SELECT * FROM users;

-- Проверка данных задач
SELECT '=== ЗАДАЧИ ===' AS '';
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    t.due_date,
    u.username as assigned_to
FROM tasks t
JOIN users u ON t.user_id = u.id;

-- Проверка уведомлений
SELECT '=== УВЕДОМЛЕНИЯ ===' AS '';
SELECT * FROM notifications;

-- Проверка комментариев
SELECT '=== КОММЕНТАРИИ ===' AS '';
SELECT * FROM task_comments;

-- Проверка истории
SELECT '=== ИСТОРИЯ ИЗМЕНЕНИЙ ===' AS '';
SELECT * FROM task_history;

-- Информация для входа
SELECT '=== ДАННЫЕ ДЛЯ ВХОДА ===' AS '';
SELECT 
    username AS 'Логин',
    password AS 'Пароль',
    role AS 'Роль',
    email AS 'Email'
FROM users;
