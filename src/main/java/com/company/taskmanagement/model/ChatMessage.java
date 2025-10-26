package com.company.taskmanagement.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class ChatMessage {
    private Long id;
    private String sender;
    private String text;
    private LocalDateTime timestamp;
    private MessageType type = MessageType.MESSAGE;
    private Set<String> reactions = new HashSet<>();
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;

    // Конструкторы
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String sender, String text) {
        this();
        this.sender = sender;
        this.text = text;
    }

    // Статический метод для системных сообщений
    public static ChatMessage system(String text) {
        ChatMessage message = new ChatMessage();
        message.sender = "System";
        message.text = text;
        message.type = MessageType.SYSTEM;
        return message;
    }

    // Статический метод для сообщений об ошибках
    public static ChatMessage error(String text) {
        ChatMessage message = new ChatMessage();
        message.sender = "System";
        message.text = text;
        message.type = MessageType.ERROR;
        return message;
    }

    // Статический метод для файловых сообщений
    public static ChatMessage file(String sender, String fileName, String fileUrl, Long fileSize, String fileType) {
        ChatMessage message = new ChatMessage();
        message.sender = sender;
        message.fileName = fileName;
        message.fileUrl = fileUrl;
        message.fileSize = fileSize;
        message.fileType = fileType;
        message.type = MessageType.FILE;
        return message;
    }

    // Enum для типа сообщения
    public enum MessageType {
        MESSAGE, SYSTEM, ERROR, FILE
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public Set<String> getReactions() { return reactions; }
    public void setReactions(Set<String> reactions) { this.reactions = reactions; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    // Вспомогательные методы
    public void addReaction(String emoji) {
        this.reactions.add(emoji);
    }

    public void removeReaction(String emoji) {
        this.reactions.remove(emoji);
    }

    public boolean isFileMessage() {
        return type == MessageType.FILE;
    }

    public String getFileIcon() {
        if (fileType == null) return "📎";
        if (fileType.startsWith("image/")) return "🖼️";
        if (fileType.contains("pdf")) return "📄";
        if (fileType.contains("word")) return "📝";
        if (fileType.contains("excel")) return "📊";
        if (fileType.contains("zip")) return "📦";
        return "📎";
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "";
        if (fileSize < 1024) return fileSize + " B";
        else if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        else return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
    }

    // Переопределяем equals и hashCode для правильного сравнения
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}