package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TelegramGroupNotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.group-id:}")
    private String groupChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Отправляет уведомление о новой задаче в группу
     */
    public boolean sendTaskNotification(ProjectTask task) {
        if (botToken == null || botToken.isEmpty() || groupChatId == null || groupChatId.isEmpty()) {
            System.out.println("❌ Telegram bot token or group chat ID not configured");
            return false;
        }

        String message = buildTaskMessage(task);
        return sendGroupMessage(message);
    }

    /**
     * Отправляет сообщение в группу (публичный метод)
     */
    public boolean sendGroupMessage(String text) {
        if (botToken == null || botToken.isEmpty() || groupChatId == null || groupChatId.isEmpty()) {
            System.out.println("❌ Telegram bot token or group chat ID not configured");
            return false;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> request = new HashMap<>();
        request.put("chat_id", groupChatId);
        request.put("text", text);
        request.put("parse_mode", "HTML");

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            boolean success = response.getStatusCode().is2xxSuccessful();

            if (success) {
                System.out.println("✅ Уведомление отправлено в группу: " + groupChatId);
            } else {
                System.out.println("❌ Ошибка отправки в группу: " + response.getBody());
            }
            return success;
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки в группу: " + e.getMessage());
            return false;
        }
    }

    /**
     * Отправляет уведомление о завершении задачи
     */
    public boolean sendTaskCompletedNotification(ProjectTask task) {
        String message = "✅ <b>ЗАДАЧА ЗАВЕРШЕНА</b>\n\n" +
                "📝 " + escapeHtml(task.getTitle()) + "\n" +
                "👤 Исполнители: " + task.getAssignees().stream()
                .map(User::getUsername)
                .collect(Collectors.joining(", ")) + "\n\n" +
                "🎉 Отличная работа!";

        return sendGroupMessage(message);
    }

    /**
     * Отправляет уведомление об изменении задачи
     */
    public boolean sendTaskUpdatedNotification(ProjectTask task) {
        String message = "✏️ <b>ЗАДАЧА ОБНОВЛЕНА</b>\n\n" +
                "📝 " + escapeHtml(task.getTitle()) + "\n" +
                getStatusIcon(task.getStatus()) + " Новый статус: " + getStatusDisplay(task.getStatus()) + "\n\n" +
                "🔗 <a href=\"" + getTaskUrl(task.getId()) + "\">Посмотреть изменения</a>";

        return sendGroupMessage(message);
    }

    /**
     * Формирует сообщение о задаче
     */
    private String buildTaskMessage(ProjectTask task) {
        String assignees = task.getAssignees().stream()
                .map(user -> "👤 " + escapeHtml(user.getUsername()))
                .collect(Collectors.joining("\n"));

        String statusIcon = getStatusIcon(task.getStatus());
        String priorityIcon = getPriorityIcon(task.getPriority());

        return "🎯 <b>НОВАЯ ЗАДАЧА</b>\n\n" +
                "📝 <b>" + escapeHtml(task.getTitle()) + "</b>\n" +
                (task.getDescription() != null && !task.getDescription().isEmpty() ?
                        "📋 " + escapeHtml(truncate(task.getDescription(), 150)) + "\n" : "") +
                statusIcon + " <b>Статус:</b> " + getStatusDisplay(task.getStatus()) + "\n" +
                priorityIcon + " <b>Приоритет:</b> " + getPriorityDisplay(task.getPriority()) + "\n" +
                (task.getDueDate() != null ?
                        "⏰ <b>Срок:</b> " + task.getDueDate() + "\n" : "") +
                "\n<b>Исполнители:</b>\n" + assignees + "\n\n" ;
    }

    // Вспомогательные методы
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String truncate(String text, int length) {
        if (text == null || text.length() <= length) return text;
        return text.substring(0, length) + "...";
    }

    private String getStatusIcon(ProjectTask.TaskStatus status) {
        if (status == null) return "❓";
        switch (status) {
            case PENDING: return "⏳";
            case IN_PROGRESS: return "🚧";
            case COMPLETED: return "✅";
            case CANCELLED: return "❌";
            default: return "📋";
        }
    }

    private String getPriorityIcon(ProjectTask.TaskPriority priority) {
        if (priority == null) return "❓";
        switch (priority) {
            case LOW: return "⬇️";
            case MEDIUM: return "↔️";
            case HIGH: return "⬆️";
            case URGENT: return "🔥";
            default: return "🎯";
        }
    }

    private String getStatusDisplay(ProjectTask.TaskStatus status) {
        if (status == null) return "Не указан";
        return status.getDisplayName();
    }

    private String getPriorityDisplay(ProjectTask.TaskPriority priority) {
        if (priority == null) return "Не указан";
        return priority.getDisplayName();
    }

    private String getTaskUrl(Long taskId) {
        return "http://localhost:8080/tasks/view/" + taskId;
    }

    /**
     * Геттер для токена (для контроллера)
     */
    public String getBotToken() {
        return botToken != null && !botToken.isEmpty() ? "Настроен" : "Не настроен";
    }
}