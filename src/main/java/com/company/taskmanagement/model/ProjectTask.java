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

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "status_changed_date")
    private LocalDate statusChangedDate;

    // Множество исполнителей
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    // Файлы - исправляем каскадные операции
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<FileAttachment> attachments = new HashSet<>();

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

    public LocalDate getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(LocalDate archivedDate) {
        this.archivedDate = archivedDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public LocalDate getStatusChangedDate() {
        return statusChangedDate;
    }

    public void setStatusChangedDate(LocalDate statusChangedDate) {
        this.statusChangedDate = statusChangedDate;
    }

    public Set<User> getAssignees() {
        return assignees;
    }

    public void setAssignees(Set<User> assignees) {
        this.assignees = assignees;
    }

    public Set<FileAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<FileAttachment> attachments) {
        this.attachments = attachments;
    }

    // Улучшенный метод для обновления статуса
    public void updateStatus(TaskStatus newStatus) {
        TaskStatus oldStatus = this.status;
        this.status = newStatus;
        this.statusChangedDate = LocalDate.now();

        if (newStatus == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            this.completedDate = LocalDate.now();
        }

        if (oldStatus == TaskStatus.COMPLETED && newStatus != TaskStatus.COMPLETED) {
            this.completedDate = null;
        }
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

    public boolean shouldAutoArchive() {
        return isCompleted() && !archived &&
                createdAt != null &&
                createdAt.isBefore(LocalDate.now().minusDays(30));
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : "";
    }

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

    public static TaskStatus[] getAllStatuses() {
        return TaskStatus.values();
    }

    public static TaskPriority[] getAllPriorities() {
        return TaskPriority.values();
    }
}