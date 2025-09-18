package com.company.taskmanagement.repository;

import com.company.taskmanagement.model.ProjectTask;
import com.company.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    // Методы для поиска задач по исполнителям
    @Query("SELECT t FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId")
    List<ProjectTask> findByAssigneeId(@Param("userId") Long userId);

    @Query("SELECT t FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.archived = true")
    List<ProjectTask> findByAssigneeIdAndArchivedTrue(@Param("userId") Long userId);

    @Query("SELECT t FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.archived = false")
    List<ProjectTask> findByAssigneeIdAndArchivedFalse(@Param("userId") Long userId);

    // Остальные методы остаются без изменений
    List<ProjectTask> findByStatus(String status);
    List<ProjectTask> findByPriority(String priority);

    // Методы для архива
    List<ProjectTask> findByArchivedTrue();
    List<ProjectTask> findByArchivedFalse();

    // Методы с @Query для полного контроля
    @Query("SELECT t FROM ProjectTask t WHERE t.archived = true")
    List<ProjectTask> findByArchivedTrueWithQuery();

    @Query("SELECT t FROM ProjectTask t WHERE t.archived = false")
    List<ProjectTask> findByArchivedFalseWithQuery();

    @Query("SELECT t FROM ProjectTask t WHERE t.status = :status AND t.archived = false")
    List<ProjectTask> findByStatusAndArchivedFalse(@Param("status") String status);

    @Query("SELECT t FROM ProjectTask t WHERE t.status = :status AND t.archived = true")
    List<ProjectTask> findByStatusAndArchivedTrue(@Param("status") String status);

    // Методы для подсчета с @Query
    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.archived = true")
    Long countArchivedTasks();

    @Query("SELECT COUNT(t) FROM ProjectTask t WHERE t.archived = false")
    Long countActiveTasks();

    @Query("SELECT COUNT(t) FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.archived = true")
    Long countArchivedTasksByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.archived = false")
    Long countActiveTasksByUserId(@Param("userId") Long userId);

    // Дополнительные полезные методы
    @Query("SELECT t FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.status = :status AND t.archived = false")
    List<ProjectTask> findByAssigneeIdAndStatusAndArchivedFalse(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT t FROM ProjectTask t JOIN t.assignees a WHERE a.id = :userId AND t.status = :status AND t.archived = true")
    List<ProjectTask> findByAssigneeIdAndStatusAndArchivedTrue(@Param("userId") Long userId, @Param("status") String status);

    // Методы для поиска по датам
    @Query("SELECT t FROM ProjectTask t WHERE t.archived = true AND t.archivedDate < :date")
    List<ProjectTask> findArchivedTasksBeforeDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM ProjectTask t WHERE t.archived = false AND t.dueDate < :date")
    List<ProjectTask> findActiveTasksWithDueDateBefore(@Param("date") LocalDate date);
}