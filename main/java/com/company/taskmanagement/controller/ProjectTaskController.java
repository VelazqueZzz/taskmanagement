package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.service.ArchiveService;
import com.company.taskmanagement.service.FileStorageService;
import com.company.taskmanagement.service.ProjectTaskService;
import com.company.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/tasks")
public class ProjectTaskController {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/create")
    public String showCreateTaskForm(Model model) {
        try {
            model.addAttribute("users", userService.getAllUsers());
            return "create-task";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки формы создания задачи: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/create")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String dueDate,
                             @RequestParam Set<Long> userIds,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
                             RedirectAttributes redirectAttributes) {

        try {
            Set<User> users = userService.getUsersByIds(userIds);
            if (users.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Необходимо выбрать хотя бы одного исполнителя!");
                return "redirect:/tasks/create";
            }

            ProjectTask task = new ProjectTask();
            task.setTitle(title);
            task.setDescription(description);
            task.setStatus(ProjectTask.TaskStatus.valueOf(status));
            task.setPriority(ProjectTask.TaskPriority.valueOf(priority));

            if (dueDate != null && !dueDate.isEmpty()) {
                task.setDueDate(LocalDate.parse(dueDate));
            }

            task.setAssignees(users);

            // 1. Сначала создаем задачу
            ProjectTask savedTask = projectTaskService.createTask(task);

            // 2. Затем сохраняем файлы через FileStorageService
            int fileCount = 0;
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        fileStorageService.storeFileForTask(file, savedTask.getId());
                        fileCount++;
                    }
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Задача успешно создана!" +
                            (fileCount > 0 ? " Загружено файлов: " + fileCount : ""));

            return "redirect:/tasks/create";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка создания задачи: " + e.getMessage());
            return "redirect:/tasks/create";
        }
    }

    @GetMapping
    public String tasks(@RequestParam(required = false) String success,
                        @RequestParam(required = false) String error,
                        Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);

            if (user != null) {
                if (user.getRole().equals("ADMIN")) {
                    model.addAttribute("tasks", archiveService.getActiveTasks());
                } else {
                    model.addAttribute("tasks", archiveService.getUserActiveTasks(user.getId()));
                }
            }

            if (success != null) {
                model.addAttribute("successMessage", getSuccessMessage(success));
            }
            if (error != null) {
                model.addAttribute("errorMessage", getErrorMessage(error));
            }

            return "tasks";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки задач: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/update/{id}")
    public String showUpdateTaskForm(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== LOADING UPDATE FORM FOR TASK " + id + " ===");

            ProjectTask task = projectTaskService.getTaskById(id).orElse(null);
            if (task == null) {
                System.out.println("TASK NOT FOUND");
                return "redirect:/tasks?error=not_found";
            }

            System.out.println("Task found: " + task.getTitle());
            System.out.println("Task status: " + task.getStatus());
            System.out.println("Task priority: " + task.getPriority());

            // Load users
            var users = userService.getAllUsers();
            System.out.println("Users loaded: " + users.size());

            // Simple model attributes
            model.addAttribute("task", task);
            model.addAttribute("users", users);

            System.out.println("=== FORM READY ===");
            return "update-task";

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/tasks?error=load";
        }
    }

    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String dueDate,
                             @RequestParam(required = false) Set<Long> userIds,
                             @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles,
                             @RequestParam(value = "deleteFiles", required = false) List<Long> filesToDelete,
                             RedirectAttributes redirectAttributes) {

        try {
            // Validate assignees
            if (userIds == null || userIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one assignee!");
                return "redirect:/tasks/update/" + id;
            }

            Set<User> users = userService.getUsersByIds(userIds);

            // Get existing task
            ProjectTask existingTask = projectTaskService.getTaskById(id).orElse(null);
            if (existingTask == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Task not found!");
                return "redirect:/tasks";
            }

            // Update basic fields
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setStatus(ProjectTask.TaskStatus.valueOf(status));
            existingTask.setPriority(ProjectTask.TaskPriority.valueOf(priority));
            existingTask.setAssignees(users);

            if (dueDate != null && !dueDate.isEmpty()) {
                existingTask.setDueDate(LocalDate.parse(dueDate));
            } else {
                existingTask.setDueDate(null);
            }

            // Update task
            ProjectTask updatedTask = projectTaskService.updateTask(id, existingTask);

            // Handle file deletion
            int deletedFilesCount = 0;
            if (filesToDelete != null && !filesToDelete.isEmpty()) {
                for (Long fileId : filesToDelete) {
                    try {
                        fileStorageService.deleteFile(fileId);
                        deletedFilesCount++;
                    } catch (IOException e) {
                        System.err.println("Error deleting file: " + e.getMessage());
                    }
                }
            }

            // Handle new file uploads
            int newFilesCount = 0;
            if (newFiles != null && !newFiles.isEmpty()) {
                for (MultipartFile file : newFiles) {
                    if (file != null && !file.isEmpty()) {
                        fileStorageService.storeFileForTask(file, updatedTask.getId());
                        newFilesCount++;
                    }
                }
            }

            // Success message
            String successMessage = "Task updated successfully!";
            if (newFilesCount > 0) {
                successMessage += " Added " + newFilesCount + " new files.";
            }
            if (deletedFilesCount > 0) {
                successMessage += " Deleted " + deletedFilesCount + " files.";
            }

            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/tasks?success=updated";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating task: " + e.getMessage());
            return "redirect:/tasks/update/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        try {
            // Сначала удаляем файлы задачи
            try {
                fileStorageService.deleteAllTaskFiles(id);
            } catch (IOException e) {
                System.err.println("Ошибка удаления файлов задачи: " + e.getMessage());
            }

            // Затем удаляем саму задачу
            projectTaskService.deleteTask(id);
            return "redirect:/tasks?success=deleted";
        } catch (Exception e) {
            return "redirect:/tasks?error=delete";
        }
    }

    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProjectTask task = projectTaskService.getTaskById(id).orElse(null);
            if (task != null) {
                task.setStatus(ProjectTask.TaskStatus.COMPLETED);
                projectTaskService.updateTask(id, task);

                if (task.getDueDate() != null &&
                        task.getDueDate().isBefore(LocalDate.now().minusMonths(1))) {
                    archiveService.archiveTask(id);
                    redirectAttributes.addAttribute("success", "completed_archived");
                } else {
                    redirectAttributes.addAttribute("success", "completed");
                }
            }
            return "redirect:/tasks";
        } catch (Exception e) {
            return "redirect:/tasks?error=complete";
        }
    }

    @GetMapping("/view/{id}")
    public String viewTask(@PathVariable Long id, Model model) {
        try {
            ProjectTask task = projectTaskService.getTaskById(id).orElse(null);
            if (task == null) {
                return "redirect:/tasks?error=not_found";
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username).orElse(null);

            if (currentUser == null) {
                return "redirect:/login";
            }

            if (currentUser.getRole().equals("USER")) {
                boolean hasAccess = task.getAssignees().stream()
                        .anyMatch(user -> user.getId().equals(currentUser.getId()));

                if (!hasAccess) {
                    return "redirect:/tasks?error=access_denied";
                }
            }

            // Загружаем файлы для задачи
            var attachments = fileStorageService.getTaskFiles(id);
            task.getAttachments().clear();
            task.getAttachments().addAll(attachments);

            model.addAttribute("task", task);
            return "view-task";

        } catch (Exception e) {
            return "redirect:/tasks?error=load";
        }
    }

    private String getSuccessMessage(String code) {
        switch (code) {
            case "updated": return "Задача успешно обновлена!";
            case "created": return "Задача успешно создана!";
            case "completed": return "Задача завершена!";
            case "deleted": return "Задача удалена!";
            default: return "Операция выполнена успешно!";
        }
    }

    private String getErrorMessage(String code) {
        switch (code) {
            case "not_found": return "Задача не найдена!";
            case "load": return "Ошибка загрузки задачи!";
            case "update": return "Ошибка обновления задачи!";
            case "delete": return "Ошибка удаления задачи!";
            case "complete": return "Ошибка завершения задачи!";
            case "create": return "Ошибка создания задачи!";
            case "no_assignees": return "Необходимо выбрать хотя бы одного исполнителя!";
            case "access_denied": return "Доступ запрещен!";
            default: return "Произошла ошибка!";
        }
    }
}