package com.company.taskmanagement.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

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

    // Обновляем связь с tasks - теперь многие ко многим
    @ManyToMany(mappedBy = "assignees", fetch = FetchType.LAZY)
    private Set<ProjectTask> tasks = new HashSet<>();

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

    public Set<ProjectTask> getTasks() { return tasks; }
    public void setTasks(Set<ProjectTask> tasks) { this.tasks = tasks; }
}