package com.company.taskmanagement.service;

import com.company.taskmanagement.model.User;
import com.company.taskmanagement.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @PostConstruct
    public void init() {
        // Создание директории для загрузок
        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        // Создаем администратора если его нет
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Plain text
            admin.setEmail("admin@company.com");
            admin.setFullName("Администратор Системы");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("✅ Администратор создан: admin/admin123");
        }

        // Создаем тестового пользователя если его нет
        if (userRepository.findByUsername("user1").isEmpty()) {
            User user = new User();
            user.setUsername("user1");
            user.setPassword("user123"); // Plain text
            user.setEmail("user1@company.com");
            user.setFullName("Тестовый Пользователь");
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("✅ Пользователь создан: user1/user123");
        }

        // Создаем второго пользователя если его нет
        if (userRepository.findByUsername("user2").isEmpty()) {
            User user = new User();
            user.setUsername("user2");
            user.setPassword("password"); // Plain text
            user.setEmail("user2@company.com");
            user.setFullName("Второй Пользователь");
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("✅ Пользователь создан: user2/password");
        }
    }

}