package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.ChatMessage;
import com.company.taskmanagement.service.ChatService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    private final String FILE_UPLOAD_DIR = "chat-uploads";

    // Создаем директорию при инициализации
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(FILE_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created chat uploads directory: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("Chat uploads directory already exists: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
        }
    }

    @GetMapping("/messages")
    public List<ChatMessage> getChatMessages(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Long afterId) {

        if (afterId != null) {
            return chatService.getMessagesAfterId(afterId);
        }
        return chatService.getRecentMessages(limit);
    }

    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestParam String text, Authentication authentication) {
        try {
            System.out.println("=== CHAT MESSAGE RECEIVED ===");
            System.out.println("From: " + authentication.getName());
            System.out.println("Text: " + text);

            ChatMessage message = new ChatMessage();
            message.setId(System.currentTimeMillis());
            message.setSender(authentication.getName());
            message.setText(text);
            message.setTimestamp(LocalDateTime.now());

            ChatMessage savedMessage = chatService.saveMessage(message);
            System.out.println("Message saved in memory: " + savedMessage.getText() + " ID: " + savedMessage.getId());

            return savedMessage;

        } catch (Exception e) {
            System.err.println("ERROR in sendMessage: " + e.getMessage());
            e.printStackTrace();
            return ChatMessage.error("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    @PostMapping("/upload-file")
    public ChatMessage uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            System.out.println("=== FILE UPLOAD STARTED ===");
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());

            if (file.isEmpty()) {
                System.err.println("File is empty");
                return ChatMessage.error("Файл пустой");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                System.err.println("File too large: " + file.getSize());
                return ChatMessage.error("Размер файла превышает 10MB");
            }

            // Проверяем и создаем директорию
            Path uploadPath = Paths.get(FILE_UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory");
            }

            // Генерируем уникальное имя файла
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = uploadPath.resolve(uniqueFilename);
            System.out.println("Saving file to: " + targetLocation.toAbsolutePath());

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Проверяем что файл сохранился
            if (!Files.exists(targetLocation)) {
                throw new IOException("File was not saved properly");
            }

            long fileSize = Files.size(targetLocation);
            System.out.println("File saved successfully. Size: " + fileSize);

            // Создаем сообщение с файлом
            ChatMessage fileMessage = ChatMessage.file(
                    authentication.getName(),
                    originalFilename,
                    "/api/chat/download/" + uniqueFilename,
                    fileSize,
                    file.getContentType()
            );
            fileMessage.setId(System.currentTimeMillis());
            fileMessage.setTimestamp(LocalDateTime.now());

            ChatMessage savedMessage = chatService.saveMessage(fileMessage);
            System.out.println("File message created with ID: " + savedMessage.getId());

            return savedMessage;

        } catch (IOException e) {
            System.err.println("File upload error: " + e.getMessage());
            e.printStackTrace();
            return ChatMessage.error("Ошибка загрузки файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during file upload: " + e.getMessage());
            e.printStackTrace();
            return ChatMessage.error("Неожиданная ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            System.out.println("Download requested for file: " + filename);

            Path filePath = Paths.get(FILE_UPLOAD_DIR).resolve(filename).normalize();
            System.out.println("Resolved file path: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                System.err.println("File not found: " + filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            if (!resource.isReadable()) {
                System.err.println("File is not readable: " + filePath.toAbsolutePath());
                return ResponseEntity.status(500).build();
            }

            // Определяем Content-Type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("File download successful. Content-Type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("File download error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/react/{messageId}")
    public ChatMessage addReaction(
            @PathVariable Long messageId,
            @RequestParam String emoji,
            Authentication authentication) {

        System.out.println("Adding reaction " + emoji + " to message " + messageId + " by " + authentication.getName());
        return chatService.addReaction(messageId, emoji, authentication.getName());
    }

    @DeleteMapping("/react/{messageId}")
    public ChatMessage removeReaction(
            @PathVariable Long messageId,
            @RequestParam String emoji,
            Authentication authentication) {

        System.out.println("Removing reaction " + emoji + " from message " + messageId + " by " + authentication.getName());
        return chatService.removeReaction(messageId, emoji, authentication.getName());
    }

    @GetMapping("/emojis")
    public Set<String> getAvailableEmojis() {
        return chatService.getAvailableEmojis();
    }

    @GetMapping("/online-users")
    public List<String> getOnlineUsers() {
        return chatService.getOnlineUsers();
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}