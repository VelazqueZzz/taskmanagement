package com.company.taskmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}