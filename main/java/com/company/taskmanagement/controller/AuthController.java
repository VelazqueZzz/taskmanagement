package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.service.ProjectTaskService;
import com.company.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectTaskService taskService;

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
                model.addAttribute("totalUsers", userService.getAllUsers().size());
                model.addAttribute("totalTasks", taskService.getAllTasks().size());
            } else {
                model.addAttribute("userTasks", taskService.getTasksByUser(currentUser));
                model.addAttribute("completedTasks", taskService.getTasksByUser(currentUser)
                        .stream().filter(task -> task.getStatus() == ProjectTask.TaskStatus.COMPLETED).count());
                model.addAttribute("pendingTasks", taskService.getTasksByUser(currentUser)
                        .stream().filter(task -> task.getStatus() == ProjectTask.TaskStatus.PENDING).count());
            }

            return "dashboard";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
}