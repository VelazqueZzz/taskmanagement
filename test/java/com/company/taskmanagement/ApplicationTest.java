package com.company.taskmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTest {

    @Test
    void contextLoads() {
        // Просто проверяем что контекст загружается
        System.out.println("✅ Контекст Spring успешно загружен");
    }
}