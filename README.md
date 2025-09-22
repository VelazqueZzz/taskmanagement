Не большой проект распределения задач в отделе или компании. 

    Стэк HTML+CSS+SpringBoot+MySql

незабудьте в application.properties выставить свои настройки

Скрипт для создания базы и таблиц:


    CREATE DATABASE task_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Используем созданную базу

    USE task_management_db;


-- Таблица пользователей

                  CREATE TABLE users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    full_name VARCHAR(255),
                    role VARCHAR(50) NOT NULL DEFAULT 'USER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                );

-- Таблица задач

    CREATE TABLE tasks (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
        priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
        due_date DATE,
        created_at DATE NOT NULL,
        is_archived BOOLEAN NOT NULL DEFAULT FALSE,
        archived_date DATE,
        INDEX idx_status (status),
        INDEX idx_priority (priority),
        INDEX idx_archived (is_archived),
        INDEX idx_due_date (due_date)
    );

-- Таблица связи многие-ко-многим для исполнителей задач

    CREATE TABLE task_assignees (
        task_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (task_id, user_id),
        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        INDEX idx_user_id (user_id)
    );

-- Индексы для улучшения производительности

    CREATE INDEX idx_tasks_created ON tasks(created_at);
    CREATE INDEX idx_tasks_archived_date ON tasks(archived_date);
    CREATE INDEX idx_users_role ON users(role);
