package com.company.taskmanagement.service;

import com.company.taskmanagement.model.FileAttachment;
import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.repository.FileAttachmentRepository;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private FileAttachmentRepository fileAttachmentRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    /**
     * Сохранить файл для задачи - ЕДИНСТВЕННЫЙ МЕТОД ДЛЯ СОХРАНЕНИЯ ФАЙЛОВ
     */
    public FileAttachment storeFileForTask(MultipartFile file, Long taskId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Файл пустой");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IOException("Размер файла превышает 10MB");
        }

        if (!isAllowedFileType(file.getContentType())) {
            throw new IOException("Тип файла не разрешен");
        }

        // Получаем задачу из репозитория
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Задача не найдена"));

        // Создание директории для задачи
        Path taskDir = Paths.get(uploadDir, "tasks", taskId.toString());
        Files.createDirectories(taskDir);

        // Генерация уникального имени файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = taskDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Сохранение информации о файле в БД
        FileAttachment attachment = new FileAttachment();
        attachment.setOriginalFilename(originalFilename);
        attachment.setFilename(uniqueFilename);
        attachment.setFilePath(targetLocation.toString());
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setTask(task);

        return fileAttachmentRepository.save(attachment);
    }

    /**
     * УДАЛЯЕМ старый метод storeFile чтобы избежать путаницы
     */
    // public FileAttachment storeFile(MultipartFile file, ProjectTask task) throws IOException {
    //     // Этот метод больше не используется
    // }

    public List<FileAttachment> getTaskFiles(Long taskId) {
        return fileAttachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);
    }

    public FileAttachment getFile(Long fileId) {
        return fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Файл не найден"));
    }

    public void deleteFile(Long fileId) throws IOException {
        FileAttachment attachment = getFile(fileId);

        // Удаление физического файла
        Files.deleteIfExists(Paths.get(attachment.getFilePath()));

        // Удаление записи из БД
        fileAttachmentRepository.delete(attachment);
    }

    public void deleteAllTaskFiles(Long taskId) throws IOException {
        List<FileAttachment> attachments = fileAttachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId);

        for (FileAttachment attachment : attachments) {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        }

        fileAttachmentRepository.deleteByTaskId(taskId);
    }

    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) return false;

        return contentType.startsWith("image/") ||
                contentType.contains("pdf") ||
                contentType.startsWith("text/") ||
                contentType.contains("word") ||
                contentType.contains("excel") ||
                contentType.contains("zip") ||
                contentType.contains("rar");
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}