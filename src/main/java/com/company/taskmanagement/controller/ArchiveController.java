package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.service.ArchiveService;
import com.company.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/archive")
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private UserService userService;

    /**
     * Показать архив задач
     */
    @GetMapping
    public String showArchive(@RequestParam(required = false) String success,
                              @RequestParam(required = false) String error,
                              Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.getUserByUsername(username).orElse(null);

            List<ProjectTask> archivedTasks;

            if (user != null && user.getRole().equals("ADMIN")) {
                // Админ видит все архивные задачи
                archivedTasks = archiveService.getArchivedTasks();
            } else if (user != null) {
                // Обычный пользователь видит только свои архивные задачи
                archivedTasks = archiveService.getUserArchivedTasks(user.getId());
            } else {
                archivedTasks = List.of();
            }

            model.addAttribute("archivedTasks", archivedTasks);

            // Сообщения
            if (success != null) {
                model.addAttribute("successMessage", getSuccessMessage(success));
            }
            if (error != null) {
                model.addAttribute("errorMessage", getErrorMessage(error));
            }

            return "archive";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки архива: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Архивировать задачу
     */
    @PostMapping("/archive/{id}")
    public String archiveTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            archiveService.archiveTask(id);
            redirectAttributes.addAttribute("success", "archived");
            return "redirect:/tasks";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "archive");
            return "redirect:/tasks";
        }
    }

    /**
     * Восстановить задачу из архива
     */
    @PostMapping("/unarchive/{id}")
    public String unarchiveTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            archiveService.unarchiveTask(id);
            redirectAttributes.addAttribute("success", "unarchived");
            return "redirect:/archive";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "unarchive");
            return "redirect:/archive";
        }
    }

    /**
     * Удалить архивную задачу
     */
    @PostMapping("/delete/{id}")
    public String deleteArchivedTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            archiveService.deleteArchivedTask(id);
            redirectAttributes.addAttribute("success", "deleted");
            return "redirect:/archive";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "delete");
            return "redirect:/archive";
        }
    }

    private String getSuccessMessage(String code) {
        switch (code) {
            case "archived": return "Задача успешно архивирована!";
            case "unarchived": return "Задача восстановлена из архива!";
            case "deleted": return "Архивная задача удалена!";
            default: return "Операция выполнена успешно!";
        }
    }

    private String getErrorMessage(String code) {
        switch (code) {
            case "archive": return "Ошибка архивирования задачи!";
            case "unarchive": return "Ошибка восстановления задачи!";
            case "delete": return "Ошибка удаления задачи!";
            default: return "Произошла ошибка!";
        }
    }
}