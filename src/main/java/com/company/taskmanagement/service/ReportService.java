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

    // BOM для UTF-8
    private static final String UTF8_BOM = "\uFEFF";

    /**
     * Генерация Excel отчета по завершенным задачам (CSV с запятыми)
     */
    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate, Long userId) {
        // Всегда используем код-фильтрацию чтобы избежать проблем с запросами
        List<ProjectTask> completedTasks = filterCompletedTasks(startDate, endDate, userId);

        System.out.println("Найдено задач после фильтрации: " + completedTasks.size());
        System.out.println("Период: " + startDate + " - " + endDate);
        System.out.println("User ID: " + userId);

        StringBuilder csv = new StringBuilder();

        // Заголовок с кириллицей
        csv.append("Пользователь,Задача,Приоритет,Дата назначения,Дата завершения\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (!completedTasks.isEmpty()) {
            for (ProjectTask task : completedTasks) {
                // ФИКС: Если задача без исполнителей, добавляем одну строку
                if (task.getAssignees() == null || task.getAssignees().isEmpty()) {
                    csv.append("Не назначен,")
                            .append(escapeCsvField(task.getTitle())).append(",")
                            .append(escapeCsvField(task.getPriority() != null ? task.getPriority().getDisplayName() : "Не указан")).append(",")
                            .append(escapeCsvField(task.getDueDate() != null ? task.getDueDate().format(formatter) : "Не указана")).append(",")
                            .append(escapeCsvField(task.getArchivedDate() != null ? task.getArchivedDate().format(formatter) : "Не указана")).append("\n");
                } else {
                    // ФИКС: Для каждой задачи добавляем строку для КАЖДОГО исполнителя
                    for (User user : task.getAssignees()) {
                        csv.append(escapeCsvField(user.getUsername())).append(",")
                                .append(escapeCsvField(task.getTitle())).append(",")
                                .append(escapeCsvField(task.getPriority() != null ? task.getPriority().getDisplayName() : "Не указан")).append(",")
                                .append(escapeCsvField(task.getDueDate() != null ? task.getDueDate().format(formatter) : "Не указана")).append(",")
                                .append(escapeCsvField(task.getArchivedDate() != null ? task.getArchivedDate().format(formatter) : "Не указана")).append("\n");
                    }
                }
            }
        } else {
            csv.append("Нет данных за указанный период,,,,\n");
        }

        String contentWithBom = UTF8_BOM + csv.toString();
        return contentWithBom.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Фильтрация завершенных задач
     */
    private List<ProjectTask> filterCompletedTasks(LocalDate startDate, LocalDate endDate, Long userId) {
        try {
            // Получаем все архивированные задачи
            List<ProjectTask> archivedTasks = projectTaskRepository.findByArchivedTrue();

            System.out.println("Всего архивированных задач: " + archivedTasks.size());

            return archivedTasks.stream()
                    .filter(task -> task != null)
                    .filter(task -> task.getStatus() != null)
                    .filter(task -> task.getStatus() == ProjectTask.TaskStatus.COMPLETED)
                    .filter(task -> task.getArchivedDate() != null)
                    .filter(task -> isDateInRange(task.getArchivedDate(), startDate, endDate))
                    .filter(task -> userId == null || userId <= 0 || isTaskAssignedToUser(task, userId))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("Ошибка фильтрации задач: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Проверка даты на вхождение в диапазон
     */
    private boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return (date.isEqual(startDate) || date.isAfter(startDate)) &&
                (date.isEqual(endDate) || date.isBefore(endDate));
    }

    /**
     * Проверка назначения задачи пользователю
     */
    private boolean isTaskAssignedToUser(ProjectTask task, Long userId) {
        if (task.getAssignees() == null) {
            return false;
        }
        return task.getAssignees().stream()
                .anyMatch(user -> user != null && user.getId() != null && user.getId().equals(userId));
    }

    /**
     * Экранирование полей для CSV
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}