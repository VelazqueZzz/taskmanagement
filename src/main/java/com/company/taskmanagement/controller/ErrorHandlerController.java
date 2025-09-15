package com.company.taskmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorHandlerController {

    @RequestMapping("/sw.js")
    public String handleServiceWorker(HttpServletRequest request) {
        // Перенаправляем запросы к sw.js на статический ресурс
        return "forward:/js/sw.js";
    }

    @RequestMapping("/favicon.ico")
    public String handleFavicon() {
        // Игнорируем запросы favicon.ico чтобы избежать ошибок
        return "forward:/";
    }
}