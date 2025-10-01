package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ArchiveService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectTaskService projectTaskService;

    /**
     * Архивировать задачу
     */
    @Transactional
    public void archiveTask(Long taskId) {
        ProjectTask task = projectTaskService.getTaskById(taskId).orElse(null);
        if (task != null && !task.isArchived()) {
            task.setArchived(true);
            projectTaskRepository.save(task);
        }
    }

    /**
     * Восстановить задачу из архива
     */
    @Transactional
    public void unarchiveTask(Long taskId) {
        ProjectTask task = projectTaskService.getTaskById(taskId).orElse(null);
        if (task != null && task.isArchived()) {
            task.setArchived(false);
            projectTaskRepository.save(task);
        }
    }

    /**
     * Удалить архивную задачу
     */
    @Transactional
    public boolean deleteArchivedTask(Long taskId) {
        try {
            ProjectTask task = projectTaskService.getTaskById(taskId).orElse(null);
            if (task != null && task.isArchived()) {
                projectTaskRepository.delete(task);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error deleting archived task: " + e.getMessage());
            return false;
        }
    }

    /**
     * Автоматическое архивирование завершенных задач через 30 дней
     */
    @Scheduled(cron = "0 0 2 * * ?") // Каждый день в 2:00
    @Transactional
    public void autoArchiveCompletedTasks() {
        LocalDate thresholdDate = LocalDate.now().minusDays(30);
        List<ProjectTask> completedTasks = projectTaskRepository
                .findByStatusAndArchivedFalse("COMPLETED");

        for (ProjectTask task : completedTasks) {
            if (task.getCreatedAt() != null &&
                    task.getCreatedAt().isBefore(thresholdDate)) {
                task.setArchived(true);
                projectTaskRepository.save(task);
            }
        }
    }

    /**
     * Получить все архивные задачи
     */
    public List<ProjectTask> getArchivedTasks() {
        return projectTaskRepository.findByArchivedTrue();
    }

    /**
     * Получить архивные задачи пользователя
     */
    public List<ProjectTask> getUserArchivedTasks(Long userId) {
        return projectTaskRepository.findByAssigneeIdAndArchivedTrue(userId);
    }


    /**
     * Получить активные задачи пользователя
     */
    public List<ProjectTask> getUserActiveTasks(Long userId) {
        return projectTaskRepository.findByAssigneeIdAndArchivedFalse(userId);
    }
    /**
     * Получить все задачи (для администратора)
     */
    public List<ProjectTask> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    /**
     * Получить неархивные задачи
     */
    public List<ProjectTask> getActiveTasks() {
        return projectTaskRepository.findByArchivedFalse();
    }

    /**
     * Получить задачи пользователя
     */
    public List<ProjectTask> getTasksByUserId(Long userId) {
        return projectTaskRepository.findByAssigneeId(userId);
    }

    /**
     * Получить количество архивных задач
     */
    public Long getArchivedTasksCount() {
        return projectTaskRepository.countArchivedTasks();
    }

    /**
     * Получить количество активных задач
     */
    public Long getActiveTasksCount() {
        return projectTaskRepository.countActiveTasks();
    }

    /**
     * Получить количество архивных задач пользователя
     */
    public Long getUserArchivedTasksCount(Long userId) {
        return projectTaskRepository.countArchivedTasksByUserId(userId);
    }

    /**
     * Получить количество активных задач пользователя
     */
    public Long getUserActiveTasksCount(Long userId) {
        return projectTaskRepository.countActiveTasksByUserId(userId);
    }

    /**
     * Получить задачи для автоматического архивирования
     */
    public List<ProjectTask> getTasksForAutoArchiving() {
        LocalDate thresholdDate = LocalDate.now().minusDays(30);
        return projectTaskRepository.findByStatusAndArchivedFalse("COMPLETED").stream()
                .filter(task -> task.getCreatedAt() != null &&
                        task.getCreatedAt().isBefore(thresholdDate))
                .collect(java.util.stream.Collectors.toList());
    }
}