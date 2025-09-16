package com.company.taskmanagement.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN or USER

    @Column(nullable = false)
    private String email;

    private String fullName;

    @OneToMany(mappedBy = "assignedUser", cascade = CascadeType.ALL)
    private List<ProjectTask> tasks; // Изменено на ProjectTask

    // Constructors
    public User() {}

    public User(String username, String password, String role, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<ProjectTask> getTasks() { return tasks; } // Изменено на ProjectTask
    public void setTasks(List<ProjectTask> tasks) { this.tasks = tasks; } // Изменено на ProjectTask
}