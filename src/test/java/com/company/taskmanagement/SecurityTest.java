package com.company.taskmanagement;

import com.company.taskmanagement.config.SecurityConfig;
import com.company.taskmanagement.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    void testSecurityConfigLoads() {
        assertNotNull(securityConfig, "SecurityConfig должен быть загружен");
    }

    @Test
    void testUserDetailsServiceLoads() {
        assertNotNull(userDetailsService, "CustomUserDetailsService должен быть загружен");
    }

    @Test
    void testPasswordEncoderExists() {
        assertNotNull(securityConfig.passwordEncoder(), "PasswordEncoder должен быть настроен");
    }
}