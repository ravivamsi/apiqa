package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_suites")
public class TestSuite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestType testType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @OneToMany(mappedBy = "testSuite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomEndpoint> customEndpoints;
    
    @OneToMany(mappedBy = "testSuite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestCase> testCases;
    
    // Constructors
    public TestSuite() {
        this.customEndpoints = new ArrayList<>();
        this.testCases = new ArrayList<>();
    }
    
    public TestSuite(String name, String description, TestType testType, String createdBy) {
        this.name = name;
        this.description = description;
        this.testType = testType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.customEndpoints = new ArrayList<>();
        this.testCases = new ArrayList<>();
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
    
    public TestType getTestType() {
        return testType;
    }
    
    public void setTestType(TestType testType) {
        this.testType = testType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<CustomEndpoint> getCustomEndpoints() {
        return customEndpoints;
    }
    
    public void setCustomEndpoints(List<CustomEndpoint> customEndpoints) {
        this.customEndpoints = customEndpoints;
    }
    
    public List<TestCase> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
