package com.company.taskmanagement.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testUserCreation() {
        User user = new User("testuser", "password123", "USER", "test@test.com", "Test User");

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("USER", user.getRole());
        assertEquals("test@test.com", user.getEmail());
        assertTrue(user.getTasks().isEmpty());
    }

    @Test
    void testProjectTaskCreation() {
        ProjectTask task = new ProjectTask();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(ProjectTask.TaskStatus.PENDING);
        task.setPriority(ProjectTask.TaskPriority.MEDIUM);
        task.setDueDate(LocalDate.now().plusDays(7));

        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals(ProjectTask.TaskStatus.PENDING, task.getStatus());
        assertFalse(task.isArchived());
        assertTrue(task.getAssignees().isEmpty());
    }

    @Test
    void testTaskOverdueCalculation() {
        ProjectTask task = new ProjectTask();
        task.setDueDate(LocalDate.now().minusDays(1)); // Вчера
        task.setStatus(ProjectTask.TaskStatus.PENDING);

        assertTrue(task.isOverdue(), "Задача должна быть просрочена");
    }

    @Test
    void testTaskDueSoonCalculation() {
        ProjectTask task = new ProjectTask();
        task.setDueDate(LocalDate.now().plusDays(2)); // Послезавтра
        task.setStatus(ProjectTask.TaskStatus.PENDING);

        assertTrue(task.isDueSoon(), "Задача должна скоро истечь");
    }

    @Test
    void testTaskStatusDisplayNames() {
        assertEquals("Ожидание", ProjectTask.TaskStatus.PENDING.getDisplayName());
        assertEquals("В процессе", ProjectTask.TaskStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Завершено", ProjectTask.TaskStatus.COMPLETED.getDisplayName());
    }

    @Test
    void testTaskPriorityDisplayNames() {
        assertEquals("Низкий", ProjectTask.TaskPriority.LOW.getDisplayName());
        assertEquals("Средний", ProjectTask.TaskPriority.MEDIUM.getDisplayName());
        assertEquals("Высокий", ProjectTask.TaskPriority.HIGH.getDisplayName());
    }

    @Test
    void testUserTaskRelationship() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        ProjectTask task = new ProjectTask();
        task.setId(1L);
        task.setTitle("Test Task");

        // Добавляем пользователя к задаче
        task.getAssignees().add(user);
        user.getTasks().add(task);

        assertEquals(1, task.getAssignees().size());
        assertEquals(1, user.getTasks().size());
        assertTrue(task.getAssignees().contains(user));
    }
}