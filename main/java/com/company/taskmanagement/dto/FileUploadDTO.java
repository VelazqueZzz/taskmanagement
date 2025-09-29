// FileUploadDTO.java
package com.company.taskmanagement.dto;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadDTO {
    private MultipartFile file;
    private Long taskId;

    // Конструкторы
    public FileUploadDTO() {}

    public FileUploadDTO(MultipartFile file, Long taskId) {
        this.file = file;
        this.taskId = taskId;
    }

    // Геттеры и сеттеры
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
}