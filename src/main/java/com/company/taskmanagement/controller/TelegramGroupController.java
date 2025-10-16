package com.company.taskmanagement.controller;

import com.company.taskmanagement.service.TelegramGroupNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/telegram")
public class TelegramGroupController {

    @Autowired
    private TelegramGroupNotificationService telegramService;

    @Value("${telegram.bot.group-id:}")
    private String groupChatId;

    /**
     * Тестовая отправка сообщения в группу
     */
    @PostMapping("/test-group-message")
    public ResponseEntity<String> testGroupMessage(@RequestParam String message) {
        try {
            boolean sent = telegramService.sendGroupMessage(
                    "🧪 <b>ТЕСТОВОЕ УВЕДОМЛЕНИЕ</b>\n\n" + message + "\n\n✅ Система уведомлений работает!"
            );

            return sent ?
                    ResponseEntity.ok("✅ Тестовое сообщение отправлено в группу") :
                    ResponseEntity.badRequest().body("❌ Ошибка отправки в группу");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }

    /**
     * Получить информацию о настройках группы
     */
    @GetMapping("/group-info")
    public ResponseEntity<String> getGroupInfo() {
        String info = "📋 <b>Настройки Telegram группы</b>\n\n" +
                "Group Chat ID: " + (groupChatId != null && !groupChatId.isEmpty() ? groupChatId : "Не настроен") + "\n" +
                "Bot Token: " + telegramService.getBotToken();

        return ResponseEntity.ok(info);
    }

    /**
     * Простой тест отправки сообщения
     */
    @GetMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        try {
            boolean sent = telegramService.sendGroupMessage(
                    "🤖 <b>Простой тест</b>\n\n" +
                            "Если вы видите это сообщение:\n" +
                            "✅ Бот работает\n" +
                            "✅ Группа настроена\n" +
                            "✅ Уведомления отправляются"
            );

            return sent ?
                    ResponseEntity.ok("✅ Тест пройден! Сообщение отправлено в группу.") :
                    ResponseEntity.badRequest().body("❌ Тест не пройден. Сообщение не отправлено.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Ошибка: " + e.getMessage());
        }
    }
}