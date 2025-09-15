package com.company.taskmanagement.service;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import com.company.taskmanagement.repository.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectTaskService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    // Существующие методы
    public List<ProjectTask> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    public Optional<ProjectTask> getTaskById(Long id) {
        return projectTaskRepository.findById(id);
    }

    public List<ProjectTask> getTasksByUser(User user) {
        return projectTaskRepository.findByAssignedUser(user);
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
            task.setAssignedUser(taskDetails.getAssignedUser());
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
}