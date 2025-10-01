package com.company.taskmanagement.repository;

import com.company.taskmanagement.model.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);

    @Query("SELECT f FROM FileAttachment f WHERE f.task.id = :taskId")
    List<FileAttachment> findByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("DELETE FROM FileAttachment f WHERE f.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(f) FROM FileAttachment f WHERE f.task.id = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);
}