package com.apiqa.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "environments")
public class Environment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String createdByName;
    
    @OneToMany(mappedBy = "environment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<EnvironmentVariable> variables;
    
    @OneToMany(mappedBy = "environment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestRun> testRuns;
    
    // Constructors
    public Environment() {
        this.variables = new ArrayList<>();
        this.testRuns = new ArrayList<>();
    }
    
    public Environment(String name, String description, String createdByName) {
        this();
        this.name = name;
        this.description = description;
        this.createdByName = createdByName;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public List<EnvironmentVariable> getVariables() {
        return variables;
    }
    
    public void setVariables(List<EnvironmentVariable> variables) {
        this.variables = variables;
    }
    
    public List<TestRun> getTestRuns() {
        return testRuns;
    }
    
    public void setTestRuns(List<TestRun> testRuns) {
        this.testRuns = testRuns;
    }
    
    @Override
    public String toString() {
        return "Environment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", createdByName='" + createdByName + '\'' +
                ", variablesCount=" + (variables != null ? variables.size() : 0) +
                '}';
    }
}
