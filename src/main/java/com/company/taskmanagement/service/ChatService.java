package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ChatService {

    // In-memory хранилище для онлайн пользователей
    private final ConcurrentHashMap<String, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();

    // In-memory хранилище для сообщений (не сохраняем в БД)
    private final List<ChatMessage> messages = new CopyOnWriteArrayList<>();
    private static final int MAX_MESSAGES = 500; // Увеличили лимит сообщений

    // Для отслеживания последнего ID сообщения
    private long lastMessageId = 0;

    // Кэш для быстрого доступа к сообщениям по ID
    private final Map<Long, ChatMessage> messageCache = new ConcurrentHashMap<>();

    // Emoji реакции
    private static final Set<String> AVAILABLE_EMOJIS = Set.of(
            "👍", "👎", "❤️", "🔥", "🥳", "👀", "🚀", "🎉"
    );

    public List<ChatMessage> getRecentMessages(int limit) {
        int startIndex = Math.max(0, messages.size() - limit);
        List<ChatMessage> recentMessages = new ArrayList<>(messages.subList(startIndex, messages.size()));
        System.out.println("Returning " + recentMessages.size() + " recent messages");
        return recentMessages;
    }

    public List<ChatMessage> getMessagesAfterId(Long lastMessageId) {
        if (lastMessageId == null) {
            return getRecentMessages(50);
        }

        return messages.stream()
                .filter(msg -> msg.getId() != null && msg.getId() > lastMessageId)
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public ChatMessage saveMessage(ChatMessage message) {
        // Ограничение длины текстового сообщения
        if (message.getText() != null && message.getText().length() > 2000) {
            message.setText(message.getText().substring(0, 2000) + "...");
        }

        // Устанавливаем timestamp если не установлен
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        // Устанавливаем уникальный ID
        if (message.getId() == null) {
            message.setId(++lastMessageId);
        }

        // Проверяем, нет ли уже такого сообщения (чтобы избежать дублирования)
        boolean messageExists = messages.stream()
                .anyMatch(m -> m.getId() != null && m.getId().equals(message.getId()));

        if (!messageExists) {
            // Добавляем сообщение в память
            messages.add(message);
            messageCache.put(message.getId(), message);

            // Ограничиваем количество сообщений в памяти
            if (messages.size() > MAX_MESSAGES) {
                ChatMessage removed = messages.remove(0);
                messageCache.remove(removed.getId());
            }

            System.out.println("Message saved to memory. Total messages: " + messages.size());
        } else {
            System.out.println("Message already exists, skipping: " + message.getId());
        }

        return message;
    }

    public ChatMessage addReaction(Long messageId, String emoji, String username) {
        ChatMessage message = messageCache.get(messageId);
        if (message != null && AVAILABLE_EMOJIS.contains(emoji)) {
            message.addReaction(emoji);
            System.out.println("Added reaction " + emoji + " to message " + messageId + " by " + username);
        }
        return message;
    }

    public ChatMessage removeReaction(Long messageId, String emoji, String username) {
        ChatMessage message = messageCache.get(messageId);
        if (message != null) {
            message.removeReaction(emoji);
            System.out.println("Removed reaction " + emoji + " from message " + messageId + " by " + username);
        }
        return message;
    }

    public ChatMessage getMessageById(Long messageId) {
        return messageCache.get(messageId);
    }

    public void clearChatHistory() {
        messages.clear();
        messageCache.clear();
        lastMessageId = 0;
        System.out.println("Chat history cleared");
    }

    // Управление онлайн статусом
    public void userConnected(String username) {
        onlineUsers.put(username, LocalDateTime.now());
        cleanupOfflineUsers();
        System.out.println("User connected: " + username);
    }

    public void userDisconnected(String username) {
        onlineUsers.remove(username);
        System.out.println("User disconnected: " + username);
    }

    public int getOnlineUsersCount() {
        cleanupOfflineUsers();
        return onlineUsers.size();
    }

    public List<String> getOnlineUsers() {
        cleanupOfflineUsers();
        return new ArrayList<>(onlineUsers.keySet());
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    private void cleanupOfflineUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        onlineUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }

    public Set<String> getAvailableEmojis() {
        return new HashSet<>(AVAILABLE_EMOJIS);
    }
}