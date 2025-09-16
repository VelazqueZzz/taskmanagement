package com.company.taskmanagement.repository;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    // Основные методы
    List<ProjectTask> findByAssignedUser(User user);
    List<ProjectTask> findByStatus(String status);
    List<ProjectTask> findByPriority(String priority);

    // Методы для архива - используем правильное имя поля 'archived'
    List<ProjectTask> findByArchivedTrue();
    List<ProjectTask> findByArchivedFalse();

    // Методы с @Query для полного контроля
    @Query("SELECT t FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.archived = true")
    List<ProjectTask> findByAssignedUserIdAndArchivedTrue(@Param("userId") Long userId);

    @Query("SELECT t FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.archived = false")
    List<ProjectTask> findByAssignedUserIdAndArchivedFalse(@Param("userId") Long userId);

    @Query("SELECT t FROM ProjectTask t WHERE t.assignedUser.id = :userId")
    List<ProjectTask> findByAssignedUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM ProjectTask t WHERE t.status = :status AND t.archived = false")
    List<ProjectTask> findByStatusAndArchivedFalse(@Param("status") String status);

    @Query("SELECT t FROM ProjectTask t WHERE t.status = :status AND t.archived = true")
    List<ProjectTask> findByStatusAndArchivedTrue(@Param("status") String status);

    // Методы для подсчета с @Query
    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.archived = true")
    Long countArchivedTasks();

    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.archived = false")
    Long countActiveTasks();

    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.archived = true")
    Long countArchivedTasksByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.archived = false")
    Long countActiveTasksByUserId(@Param("userId") Long userId);

    // Дополнительные полезные методы
    @Query("SELECT t FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.status = :status AND t.archived = false")
    List<ProjectTask> findByAssignedUserIdAndStatusAndArchivedFalse(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT t FROM ProjectTask t WHERE t.assignedUser.id = :userId AND t.status = :status AND t.archived = true")
    List<ProjectTask> findByAssignedUserIdAndStatusAndArchivedTrue(@Param("userId") Long userId, @Param("status") String status);

    // Методы для поиска по датам
    @Query("SELECT t FROM ProjectTask t WHERE t.archived = true AND t.archivedDate < :date")
    List<ProjectTask> findArchivedTasksBeforeDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM ProjectTask t WHERE t.archived = false AND t.dueDate < :date")
    List<ProjectTask> findActiveTasksWithDueDateBefore(@Param("date") LocalDate date);
}