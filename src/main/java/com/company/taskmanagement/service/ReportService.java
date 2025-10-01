package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private UserService userService;

    private static final String UTF8_BOM = "\uFEFF";

    /**
     * Генерация расширенного отчета по всем задачам
     */
    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, Long userId) {
        // Валидация параметров
        validateReportParameters(startDate, endDate);

        // Получаем задачи для отчета
        List<ProjectTask> reportTasks = getTasksForReport(startDate, endDate, userId);

        System.out.println("Сгенерирован отчет для " + reportTasks.size() + " задач");
        System.out.println("Период: " + startDate + " - " + endDate);
        System.out.println("User ID: " + userId);

        // Генерация CSV
        String csvContent = generateCsvContent(reportTasks, startDate, endDate, userId);
        return (UTF8_BOM + csvContent).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Получить задачи для отчета с улучшенной фильтрацией
     */
    private List<ProjectTask> getTasksForReport(LocalDate startDate, LocalDate endDate, Long userId) {
        try {
            // Базовый запрос - все задачи
            List<ProjectTask> allTasks = projectTaskRepository.findAll();

            return allTasks.stream()
                    .filter(task -> task != null)
                    .filter(task -> isTaskInDateRange(task, startDate, endDate))
                    .filter(task -> userId == null || isTaskAssignedToUser(task, userId))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Ошибка при получении задач для отчета: " + e.getMessage());
            throw new RuntimeException("Не удалось сгенерировать отчет: " + e.getMessage(), e);
        }
    }

    /**
     * Проверка что задача попадает в диапазон дат
     */
    private boolean isTaskInDateRange(ProjectTask task, LocalDate startDate, LocalDate endDate) {
        // Проверяем по дате создания ИЛИ дате завершения ИЛИ дате архивации
        boolean createdInRange = task.getCreatedAt() != null &&
                isDateInRange(task.getCreatedAt(), startDate, endDate);

        boolean completedInRange = task.getCompletedDate() != null &&
                isDateInRange(task.getCompletedDate(), startDate, endDate);

        boolean archivedInRange = task.getArchivedDate() != null &&
                isDateInRange(task.getArchivedDate(), startDate, endDate);

        return createdInRange || completedInRange || archivedInRange;
    }

    /**
     * Генерация содержимого CSV
     */
    private String generateCsvContent(List<ProjectTask> tasks, LocalDate startDate, LocalDate endDate, Long userId) {
        StringBuilder csv = new StringBuilder();

        // Заголовок отчета
        csv.append("Тип задачи,Статус,Исполнитель,Название задачи,Приоритет,Дата создания,Срок выполнения,Фактическая дата завершения,Дата архивации,Просрочена\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (!tasks.isEmpty()) {
            for (ProjectTask task : tasks) {
                addTaskToCsv(csv, task, formatter);
            }
        } else {
            csv.append("Нет данных за указанный период,,,,,,,,\n");
        }

        // Добавляем информацию о фильтрах
        csv.append("\nПараметры отчета:\n");
        csv.append("Период:,").append(startDate.format(formatter)).append(" - ").append(endDate.format(formatter)).append("\n");
        if (userId != null) {
            User user = userService.getUserById(userId).orElse(null);
            csv.append("Пользователь:,").append(user != null ? user.getUsername() : "ID: " + userId).append("\n");
        } else {
            csv.append("Пользователь:,Все пользователи\n");
        }
        csv.append("Всего задач:,").append(tasks.size()).append("\n");
        csv.append("Сгенерирован:,").append(LocalDate.now().format(formatter)).append("\n");

        return csv.toString();
    }

    /**
     * Добавить задачу в CSV
     */
    private void addTaskToCsv(StringBuilder csv, ProjectTask task, DateTimeFormatter formatter) {
        String taskType = task.isArchived() ? "Архив" : "Активная";
        String statusDisplay = getStatusDisplayName(task.getStatus());
        String priorityDisplay = getPriorityDisplayName(task.getPriority());
        String createdDate = formatDate(task.getCreatedAt(), formatter);
        String dueDate = formatDate(task.getDueDate(), formatter);
        String completedDate = getActualCompletionDate(task, formatter); // Используем исправленный метод
        String archivedDate = formatDate(task.getArchivedDate(), formatter);
        String isOverdue = task.isOverdue() ? "Да" : "Нет";

        // Определяем исполнителя
        String assignee = "Не назначен";
        if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {
            // Берем первого исполнителя для отчета
            assignee = task.getAssignees().iterator().next().getUsername();
        }

        csv.append(escapeCsvField(taskType)).append(",")
                .append(escapeCsvField(statusDisplay)).append(",")
                .append(escapeCsvField(assignee)).append(",")
                .append(escapeCsvField(task.getTitle())).append(",")
                .append(escapeCsvField(priorityDisplay)).append(",")
                .append(escapeCsvField(createdDate)).append(",")
                .append(escapeCsvField(dueDate)).append(",")
                .append(escapeCsvField(completedDate)).append(",")
                .append(escapeCsvField(archivedDate)).append(",")
                .append(escapeCsvField(isOverdue)).append("\n");
    }

    /**
     * Получить фактическую дату завершения задачи
     */
    private String getActualCompletionDate(ProjectTask task, DateTimeFormatter formatter) {
        // 1. Приоритет: используем completed_date если он есть
        if (task.getCompletedDate() != null) {
            return task.getCompletedDate().format(formatter);
        }

        // 2. Для архивных задач используем дату архивации как дату завершения
        if (task.isArchived() && task.getArchivedDate() != null) {
            return task.getArchivedDate().format(formatter);
        }

        // 3. Для завершенных задач без даты завершения
        if (task.getStatus() != null && task.getStatus().toString().equals("COMPLETED")) {
            // Пытаемся использовать дату изменения статуса
            if (task.getStatusChangedDate() != null) {
                return task.getStatusChangedDate().format(formatter);
            }
            // Или дату создания
            if (task.getCreatedAt() != null) {
                return task.getCreatedAt().format(formatter);
            }
            return "Дата неизвестна";
        }

        // 4. Для задач в процессе
        if (task.getStatus() != null && task.getStatus().toString().equals("IN_PROGRESS")) {
            return "В процессе";
        }

        // 5. Для задач в ожидании
        if (task.getStatus() != null && task.getStatus().toString().equals("PENDING")) {
            return "Ожидание";
        }

        // 6. Для отмененных задач
        if (task.getStatus() != null && task.getStatus().toString().equals("CANCELLED")) {
            return "Отменена";
        }

        return "Не определена";
    }

    /**
     * Валидация параметров отчета
     */
    private void validateReportParameters(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Дата начала и окончания не могут быть пустыми");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        if (endDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата окончания не может быть в будущем");
        }
    }

    /**
     * Вспомогательные методы
     */
    private String getStatusDisplayName(ProjectTask.TaskStatus status) {
        if (status == null) return "Не указан";
        return status.getDisplayName();
    }

    private String getPriorityDisplayName(ProjectTask.TaskPriority priority) {
        if (priority == null) return "Не указан";
        return priority.getDisplayName();
    }

    private String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "Не указана";
    }

    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return date != null &&
                (date.isEqual(startDate) || date.isAfter(startDate)) &&
                (date.isEqual(endDate) || date.isBefore(endDate));
    }

    private boolean isTaskAssignedToUser(ProjectTask task, Long userId) {
        return task.getAssignees() != null &&
                task.getAssignees().stream()
                        .anyMatch(user -> user != null && user.getId() != null && user.getId().equals(userId));
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Генерация отчета по конкретному пользователю
     */
    public byte[] generateUserReport(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return generateExcelReport(startDate, endDate, userId);
    }
}