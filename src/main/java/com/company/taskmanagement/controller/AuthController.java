package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.service.ProjectTaskService;
import com.company.taskmanagement.service.UserService;
import com.company.taskmanagement.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectTaskService taskService;

    @Autowired
    private ArchiveService archiveService;

    @GetMapping("/chat")
    public String chatPage(Model model, Authentication authentication) {
        try {
            // Текущий пользователь
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            model.addAttribute("currentUser", currentUser);
            return "chat";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки чата: " + e.getMessage());
            return "error";
        }
    }
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.getUserByUsername(username).orElse(null);

            if (currentUser == null) {
                return "redirect:/login";
            }

            model.addAttribute("currentUser", currentUser);

            if (currentUser.getRole().equals("ADMIN")) {
                // Для администратора
                model.addAttribute("totalUsers", userService.getAllUsers().size());
                model.addAttribute("totalTasks", taskService.getAllTasks().size());

                // Получаем реальные данные о задачах
                List<ProjectTask> allTasks = taskService.getAllTasks();
                long activeTasksCount = allTasks.stream()
                        .filter(task -> !task.isArchived())
                        .count();
                long archivedTasksCount = allTasks.stream()
                        .filter(task -> task.isArchived())
                        .count();

                model.addAttribute("totalActiveTasks", activeTasksCount);
                model.addAttribute("totalArchivedTasks", archivedTasksCount);

            } else {
                // Для обычного пользователя
                List<ProjectTask> userTasks = taskService.getTasksByUser(currentUser);
                List<ProjectTask> userActiveTasks = archiveService.getUserActiveTasks(currentUser.getId());
                List<ProjectTask> userArchivedTasks = archiveService.getUserArchivedTasks(currentUser.getId());

                model.addAttribute("userTasks", userTasks);
                model.addAttribute("userActiveTasks", userActiveTasks);
                model.addAttribute("userArchivedTasks", userArchivedTasks);

                long completedTasks = userTasks.stream()
                        .filter(task -> task.getStatus() == ProjectTask.TaskStatus.COMPLETED)
                        .count();
                long pendingTasks = userTasks.stream()
                        .filter(task -> task.getStatus() == ProjectTask.TaskStatus.PENDING)
                        .count();

                model.addAttribute("completedTasks", completedTasks);
                model.addAttribute("pendingTasks", pendingTasks);
                model.addAttribute("totalActiveTasks", userActiveTasks.size());
                model.addAttribute("totalArchivedTasks", userArchivedTasks.size());
            }

            return "dashboard";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
}