package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProjectTaskService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TelegramGroupNotificationService telegramNotificationService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Получить все задачи
     */
    public List<ProjectTask> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    /**
     * Найти задачу по ID с проверкой существования
     */
    public Optional<ProjectTask> getTaskById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return projectTaskRepository.findById(id);
    }

    /**
     * Получить задачи пользователя
     */
    public List<ProjectTask> getTasksByUser(User user) {
        return projectTaskRepository.findByAssigneeId(user.getId());
    }

    /**
     * Получить задачи пользователя по статусу
     */
    public List<ProjectTask> getTasksByUserAndStatus(User user, ProjectTask.TaskStatus status) {
        return projectTaskRepository.findByAssigneeIdAndStatusAndArchivedFalse(user.getId(), status.toString());
    }

    /**
     * Создать новую задачу с уведомлением
     */
    public ProjectTask createTask(ProjectTask task) {
        validateTask(task);

        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDate.now());
        }
        task.setStatusChangedDate(LocalDate.now());

        ProjectTask savedTask = projectTaskRepository.save(task);

        // Отправляем уведомление в Telegram группу
        try {
            telegramNotificationService.sendTaskNotification(savedTask);
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки уведомления: " + e.getMessage());
        }

        return savedTask;
    }

    /**
     * Обновить задачу с логикой дат завершения
     */
    public ProjectTask updateTask(Long id, ProjectTask taskDetails) {
        return projectTaskRepository.findById(id).map(existingTask -> {
            ProjectTask.TaskStatus oldStatus = existingTask.getStatus();

            // Обновляем поля
            existingTask.setTitle(taskDetails.getTitle());
            existingTask.setDescription(taskDetails.getDescription());
            existingTask.setPriority(taskDetails.getPriority());
            existingTask.setDueDate(taskDetails.getDueDate());
            existingTask.setAssignees(taskDetails.getAssignees());

            // Обновляем статус
            updateTaskStatus(existingTask, taskDetails.getStatus(), oldStatus);

            ProjectTask savedTask = projectTaskRepository.save(existingTask);

            // Уведомление об обновлении
            try {
                telegramNotificationService.sendTaskUpdatedNotification(savedTask);
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки уведомления об обновлении: " + e.getMessage());
            }

            return savedTask;
        }).orElseThrow(() -> new IllegalArgumentException("Задача с ID " + id + " не найдена"));
    }

    /**
     * Обновление статуса задачи с логикой дат
     */
    private void updateTaskStatus(ProjectTask task, ProjectTask.TaskStatus newStatus, ProjectTask.TaskStatus oldStatus) {
        if (newStatus != oldStatus) {
            task.setStatus(newStatus);
            task.setStatusChangedDate(LocalDate.now());

            // Логика для даты завершения
            if (newStatus == ProjectTask.TaskStatus.COMPLETED) {
                // Устанавливаем дату завершения только если она еще не установлена
                if (task.getCompletedDate() == null) {
                    task.setCompletedDate(LocalDate.now());
                }
            } else if (oldStatus == ProjectTask.TaskStatus.COMPLETED) {
                // Если статус меняется с COMPLETED на другой - очищаем дату завершения
                task.setCompletedDate(null);
            }
        } else {
            task.setStatus(newStatus);
        }
    }

    /**
     * Завершить задачу - устанавливаем дату завершения
     */
    public ProjectTask completeTask(Long taskId) {
        return projectTaskRepository.findById(taskId).map(task -> {
            ProjectTask.TaskStatus oldStatus = task.getStatus();

            task.setStatus(ProjectTask.TaskStatus.COMPLETED);
            task.setStatusChangedDate(LocalDate.now());

            if (oldStatus != ProjectTask.TaskStatus.COMPLETED) {
                task.setCompletedDate(LocalDate.now());
            }

            ProjectTask savedTask = projectTaskRepository.save(task);

            // Уведомление о завершении
            try {
                telegramNotificationService.sendTaskCompletedNotification(savedTask);
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки уведомления о завершении: " + e.getMessage());
            }

            return savedTask;
        }).orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));
    }

    /**
     * Удалить задачу с проверкой существования и очисткой файлов
     */
    @Transactional
    public boolean deleteTask(Long id) {
        try {
            Optional<ProjectTask> taskOptional = projectTaskRepository.findById(id);
            if (taskOptional.isEmpty()) {
                System.err.println("Задача с ID " + id + " не найдена");
                return false;
            }

            ProjectTask task = taskOptional.get();
            System.out.println("Начинаем удаление задачи ID: " + id);

            // 1. Сначала очищаем связи многие-ко-многим
            if (task.getAssignees() != null) {
                System.out.println("Очищаем связи с исполнителями...");
                task.getAssignees().clear();
                projectTaskRepository.save(task); // Сохраняем без исполнителей
            }

            // 2. Удаляем файлы через сервис
            try {
                System.out.println("Удаляем файлы задачи...");
                fileStorageService.deleteAllTaskFiles(id);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении файлов задачи " + id + ": " + e.getMessage());
                // Продолжаем удаление даже если файлы не удалились
            }

            // 3. Удаляем саму задачу
            System.out.println("Удаляем задачу из базы данных...");
            projectTaskRepository.delete(task);

            // 4. Проверяем что задача удалена
            boolean stillExists = projectTaskRepository.existsById(id);
            if (stillExists) {
                System.err.println("Задача " + id + " все еще существует после удаления!");
                return false;
            }

            System.out.println("✅ Задача " + id + " успешно удалена");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Ошибка при удалении задачи " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Назначить пользователей на задачу
     */
    public ProjectTask assignUsersToTask(Long taskId, Set<Long> userIds) {
        ProjectTask task = getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));

        Set<User> users = userService.getUsersByIds(userIds);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Не указаны пользователи для назначения");
        }

        task.setAssignees(users);
        return projectTaskRepository.save(task);
    }

    /**
     * Получить исполнителей задачи
     */
    public Set<User> getTaskAssignees(Long taskId) {
        ProjectTask task = getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача с ID " + taskId + " не найдена"));
        return task.getAssignees();
    }

    /**
     * Валидация задачи перед сохранением
     */
    private void validateTask(ProjectTask task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым");
        }
        if (task.getStatus() == null) {
            task.setStatus(ProjectTask.TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(ProjectTask.TaskPriority.MEDIUM);
        }
    }

    /**
     * Восстановить даты завершения для существующих завершенных задач
     */
    @Transactional
    public void fixCompletedDates() {
        List<ProjectTask> completedTasks = projectTaskRepository.findByStatusAndArchivedFalse("COMPLETED");

        int fixedCount = 0;
        for (ProjectTask task : completedTasks) {
            if (task.getCompletedDate() == null) {
                // Используем дату изменения статуса или дату создания как приблизительную дату завершения
                if (task.getStatusChangedDate() != null) {
                    task.setCompletedDate(task.getStatusChangedDate());
                } else {
                    task.setCompletedDate(task.getCreatedAt());
                }
                projectTaskRepository.save(task);
                fixedCount++;
                System.out.println("Исправлена дата завершения для задачи ID: " + task.getId());
            }
        }
        System.out.println("Всего исправлено задач: " + fixedCount);
    }

    /**
     * Получить статистику по задачам
     */
    public TaskStatistics getTaskStatistics(Long userId) {
        TaskStatistics stats = new TaskStatistics();

        if (userId != null) {
            stats.setTotalTasks(projectTaskRepository.countActiveTasksByUserId(userId) +
                    projectTaskRepository.countArchivedTasksByUserId(userId));
            stats.setActiveTasks(projectTaskRepository.countActiveTasksByUserId(userId));
            stats.setArchivedTasks(projectTaskRepository.countArchivedTasksByUserId(userId));
            stats.setCompletedTasks(projectTaskRepository.findByAssigneeIdAndStatusAndArchivedFalse(userId, "COMPLETED").size());
        } else {
            stats.setTotalTasks(projectTaskRepository.count());
            stats.setActiveTasks(projectTaskRepository.countActiveTasks());
            stats.setArchivedTasks(projectTaskRepository.countArchivedTasks());
        }

        return stats;
    }

    /**
     * Класс для статистики задач
     */
    public static class TaskStatistics {
        private long totalTasks;
        private long activeTasks;
        private long archivedTasks;
        private long completedTasks;

        // геттеры и сеттеры
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        public long getActiveTasks() { return activeTasks; }
        public void setActiveTasks(long activeTasks) { this.activeTasks = activeTasks; }
        public long getArchivedTasks() { return archivedTasks; }
        public void setArchivedTasks(long archivedTasks) { this.archivedTasks = archivedTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    }
}