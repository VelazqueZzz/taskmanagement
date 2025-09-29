package com.company.taskmanagement.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // Просто возвращаем пароль как есть (без хэширования)
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Простое сравнение строк
        return rawPassword.toString().equals(encodedPassword);
    }
}