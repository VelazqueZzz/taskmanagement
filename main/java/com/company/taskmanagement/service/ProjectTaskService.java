package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProjectTaskService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private UserService userService;

    // УБИРАЕМ зависимость от FileStorageService чтобы избежать циклической зависимости
    // Работу с файлами выносим в контроллер

    public List<ProjectTask> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    public Optional<ProjectTask> getTaskById(Long id) {
        return projectTaskRepository.findById(id);
    }

    public List<ProjectTask> getTasksByUser(User user) {
        return projectTaskRepository.findByAssigneeId(user.getId());
    }

    public ProjectTask createTask(ProjectTask task) {
        return projectTaskRepository.save(task);
    }

    public ProjectTask updateTask(Long id, ProjectTask taskDetails) {
        return projectTaskRepository.findById(id).map(task -> {
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setStatus(taskDetails.getStatus());
            task.setPriority(taskDetails.getPriority());
            task.setDueDate(taskDetails.getDueDate());
            task.setAssignees(taskDetails.getAssignees());
            task.setArchived(taskDetails.isArchived());
            task.setArchivedDate(taskDetails.getArchivedDate());
            return projectTaskRepository.save(task);
        }).orElse(null);
    }

    public boolean deleteTask(Long id) {
        if (projectTaskRepository.existsById(id)) {
            projectTaskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ProjectTask assignUsersToTask(Long taskId, Set<Long> userIds) {
        ProjectTask task = getTaskById(taskId).orElse(null);
        if (task != null) {
            Set<User> users = userService.getUsersByIds(userIds);
            task.setAssignees(users);
            return projectTaskRepository.save(task);
        }
        return null;
    }

    public Set<User> getTaskAssignees(Long taskId) {
        ProjectTask task = getTaskById(taskId).orElse(null);
        return task != null ? task.getAssignees() : null;
    }
}