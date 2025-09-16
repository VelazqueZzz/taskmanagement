package com.company.taskmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class ProjectTask {

    // Enum для статусов задач
    public enum TaskStatus {
        PENDING("Ожидание"),
        IN_PROGRESS("В процессе"),
        COMPLETED("Завершено"),
        CANCELLED("Отменено");

        private final String displayName;

        TaskStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    // Enum для приоритетов задач
    public enum TaskPriority {
        LOW("Низкий"),
        MEDIUM("Средний"),
        HIGH("Высокий"),
        URGENT("Срочный");

        private final String displayName;

        TaskPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return this.name();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @Column(name = "archived_date")
    private LocalDate archivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User assignedUser;

    // Конструкторы
    public ProjectTask() {
        this.status = TaskStatus.PENDING;
        this.priority = TaskPriority.MEDIUM;
        this.createdAt = LocalDate.now();
        this.archived = false;
    }

    public ProjectTask(String title, String description, TaskStatus status,
                       TaskPriority priority, LocalDate dueDate, User assignedUser) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedUser = assignedUser;
        this.createdAt = LocalDate.now();
        this.archived = false;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    // Методы для архивации
    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
        if (archived) {
            this.archivedDate = LocalDate.now();
        } else {
            this.archivedDate = null;
        }
    }

    // Геттер для Thymeleaf (с префиксом get)
    public boolean getArchived() {
        return archived;
    }

    public LocalDate getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(LocalDate archivedDate) {
        this.archivedDate = archivedDate;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    // Вспомогательные методы
    public boolean isOverdue() {
        return dueDate != null &&
                dueDate.isBefore(LocalDate.now()) &&
                status != TaskStatus.COMPLETED &&
                status != TaskStatus.CANCELLED;
    }

    public boolean isDueSoon() {
        return dueDate != null &&
                dueDate.isAfter(LocalDate.now()) &&
                dueDate.isBefore(LocalDate.now().plusDays(3)) &&
                status != TaskStatus.COMPLETED &&
                status != TaskStatus.CANCELLED;
    }

    public boolean isActive() {
        return !archived;
    }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public boolean canBeArchived() {
        return isCompleted() && !archived;
    }

    public boolean canBeRestored() {
        return archived;
    }

    /**
     * Автоматически архивирует задачу если она завершена и старая
     */
    public boolean shouldAutoArchive() {
        return isCompleted() && !archived &&
                createdAt != null &&
                createdAt.isBefore(LocalDate.now().minusDays(30));
    }

    /**
     * Получить отображаемое имя статуса
     */
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    /**
     * Получить отображаемое имя приоритета
     */
    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : "";
    }

    @Override
    public String toString() {
        return "ProjectTask{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", archived=" + archived +
                ", archivedDate=" + archivedDate +
                ", assignedUser=" + (assignedUser != null ? assignedUser.getUsername() : "null") +
                '}';
    }

    // Статические методы-помощники
    public static String getStatusDisplayName(TaskStatus status) {
        return status != null ? status.getDisplayName() : "";
    }

    public static String getPriorityDisplayName(TaskPriority priority) {
        return priority != null ? priority.getDisplayName() : "";
    }

    /**
     * Получить массив всех статусов
     */
    public static TaskStatus[] getAllStatuses() {
        return TaskStatus.values();
    }

    /**
     * Получить массив всех приоритетов
     */
    public static TaskPriority[] getAllPriorities() {
        return TaskPriority.values();
    }
}