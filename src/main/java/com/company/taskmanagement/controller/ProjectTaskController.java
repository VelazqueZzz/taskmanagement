package com.company.taskmanagement.controller;

import com.company.taskmanagement.service.ArchiveService;
import com.company.taskmanagement.service.ProjectTaskService;
import com.company.taskmanagement.service.UserService;
import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class ProjectTaskController {

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArchiveService archiveService;

    /**
     * Показать форму создания новой задачи
     */
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

    /**
     * Создать новую задачу
     */
    @PostMapping("/create")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String dueDate,
                             @RequestParam Long userId,
                             RedirectAttributes redirectAttributes) {

        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Пользователь не найден!");
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

            task.setAssignedUser(user);
            projectTaskService.createTask(task);

            redirectAttributes.addFlashAttribute("successMessage", "Задача успешно создана!");
            return "redirect:/tasks/create";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Неверный формат данных: " + e.getMessage());
            return "redirect:/tasks/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка создания задачи: " + e.getMessage());
            return "redirect:/tasks/create";
        }
    }

    /**
     * Показать список всех задач
     */
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
                    // Админ видит все неархивные задачи
                    model.addAttribute("tasks", archiveService.getActiveTasks());
                } else {
                    // Пользователь видит только свои неархивные задачи
                    model.addAttribute("tasks", archiveService.getUserActiveTasks(user.getId()));
                }
            }

            // Добавляем сообщения
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

    /**
     * Показать форму редактирования задачи
     */
    @GetMapping("/update/{id}")
    public String showUpdateTaskForm(@PathVariable Long id, Model model) {
        try {
            ProjectTask task = projectTaskService.getTaskById(id).orElse(null);
            if (task == null) {
                return "redirect:/tasks?error=not_found";
            }

            model.addAttribute("task", task);
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("statuses", ProjectTask.TaskStatus.values());
            model.addAttribute("priorities", ProjectTask.TaskPriority.values());

            return "update-task";
        } catch (Exception e) {
            return "redirect:/tasks?error=load";
        }
    }

    /**
     * Обновить задачу
     */
    @PostMapping("/update/{id}")
    public String updateTask(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam String status,
                             @RequestParam String priority,
                             @RequestParam(required = false) String dueDate,
                             @RequestParam Long userId) {

        try {
            User user = userService.getUserById(userId).orElse(null);
            ProjectTask existingTask = projectTaskService.getTaskById(id).orElse(null);

            if (existingTask != null && user != null) {
                existingTask.setTitle(title);
                existingTask.setDescription(description);
                existingTask.setStatus(ProjectTask.TaskStatus.valueOf(status));
                existingTask.setPriority(ProjectTask.TaskPriority.valueOf(priority));

                if (dueDate != null && !dueDate.isEmpty()) {
                    existingTask.setDueDate(LocalDate.parse(dueDate));
                } else {
                    existingTask.setDueDate(null);
                }

                existingTask.setAssignedUser(user);
                projectTaskService.updateTask(id, existingTask);
            }
            return "redirect:/tasks?success=updated";
        } catch (Exception e) {
            return "redirect:/tasks/update/" + id + "?error=update";
        }
    }

    /**
     * Удалить задачу
     */
    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        try {
            projectTaskService.deleteTask(id);
            return "redirect:/tasks?success=deleted";
        } catch (Exception e) {
            return "redirect:/tasks?error=delete";
        }
    }

    /**
     * Завершить задачу
     */
    @PostMapping("/complete/{id}")
    public String completeTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProjectTask task = projectTaskService.getTaskById(id).orElse(null);
            if (task != null) {
                task.setStatus(ProjectTask.TaskStatus.COMPLETED);
                projectTaskService.updateTask(id, task);

                // Автоматически архивируем старые завершенные задачи
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

            // Проверяем права доступа
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username).orElse(null);

            if (currentUser == null) {
                return "redirect:/login";
            }

            // Проверяем, что пользователь имеет доступ к задаче
            if (currentUser.getRole().equals("USER") &&
                    !task.getAssignedUser().getId().equals(currentUser.getId())) {
                return "redirect:/tasks?error=access_denied";
            }

            model.addAttribute("task", task);
            return "view-task";
        } catch (Exception e) {
            return "redirect:/tasks?error=load";
        }
    }

    /**
     * Вспомогательный метод для получения сообщений об успехе
     */
    private String getSuccessMessage(String code) {
        switch (code) {
            case "updated": return "Задача успешно обновлена!";
            case "created": return "Задача успешно создана!";
            case "completed": return "Задача завершена!";
            case "deleted": return "Задача удалена!";
            default: return "Операция выполнена успешно!";
        }
    }

    /**
     * Вспомогательный метод для получения сообщений об ошибках
     */
    private String getErrorMessage(String code) {
        switch (code) {
            case "not_found": return "Задача не найдена!";
            case "load": return "Ошибка загрузки задачи!";
            case "update": return "Ошибка обновления задачи!";
            case "delete": return "Ошибка удаления задачи!";
            case "complete": return "Ошибка завершения задачи!";
            case "create": return "Ошибка создания задачи!";
            default: return "Произошла ошибка!";
        }
    }
}