package com.company.taskmanagement.controller;

import com.company.taskmanagement.model.User;
import com.company.taskmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String usersManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String fullName) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        user.setFullName(fullName);

        userService.createUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                return "redirect:/admin/users?error=not_found";
            }

            model.addAttribute("user", user);
            return "edit-user";
        } catch (Exception e) {
            return "redirect:/admin/users?error=load";
        }
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String role,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) String fullName) {

        try {
            User user = userService.getUserById(id).orElse(null);
            if (user != null) {
                user.setUsername(username);
                user.setEmail(email);
                user.setRole(role);
                user.setFullName(fullName);

                // Обновляем пароль только если он указан
                if (password != null && !password.trim().isEmpty()) {
                    user.setPassword(password);
                }

                userService.updateUser(id, user);
            }
            return "redirect:/admin/users?success=updated";
        } catch (Exception e) {
            return "redirect:/admin/users/edit/" + id + "?error=update";
        }
    }
}
