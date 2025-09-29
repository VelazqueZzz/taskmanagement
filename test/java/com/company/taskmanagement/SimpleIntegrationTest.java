package com.company.taskmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SimpleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLoginPageAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Вход")))
                .andExpect(content().string(containsString("Логин")));
    }

    @Test
    void testCssAndJsResourcesAccessible() throws Exception {
        mockMvc.perform(get("/css/style.css"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/js/app.js"))
                .andExpect(status().isOk());
    }

    @Test
    void testRootRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void testSecuredPagesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection()) // Перенаправление на login
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().is3xxRedirection());
    }
}