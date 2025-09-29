package com.company.taskmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    // Множество исполнителей
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    // Конструкторы
    public ProjectTask() {
        this.status = TaskStatus.PENDING;
        this.priority = TaskPriority.MEDIUM;
        this.createdAt = LocalDate.now();
        this.archived = false;
    }

    public ProjectTask(String title, String description, TaskStatus status,
                       TaskPriority priority, LocalDate dueDate, Set<User> assignees) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignees = assignees;
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

    public Set<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(Set<User> assignees) {
        this.assignees = assignees;
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
                ", assignees=" + (assignees != null ? assignees.size() + " users" : "null") +
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
    // Добавить в класс ProjectTask.java (в конец класса, перед закрывающей фигурной скобкой)

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FileAttachment> attachments = new HashSet<>();

    public Set<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<FileAttachment> attachments) {
        this.attachments = attachments;
    }

    // Вспомогательный метод
    public int getAttachmentCount() {
        return attachments != null ? attachments.size() : 0;
    }

    public String getFormattedTotalAttachmentSize() {
        if (attachments == null || attachments.isEmpty()) {
            return "0 B";
        }

        long totalSize = attachments.stream()
                .mapToLong(FileAttachment::getFileSize)
                .sum();

        if (totalSize < 1024) return totalSize + " B";
        else if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
        else return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
    }


}