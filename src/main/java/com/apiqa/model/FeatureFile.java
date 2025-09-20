package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feature_files")
public class FeatureFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestSuiteType suiteType;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private LocalDateTime generatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_spec_id", nullable = false)
    private ApiSpec apiSpec;
    
    @OneToMany(mappedBy = "featureFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestScenario> testScenarios;
    
    // Constructors
    public FeatureFile() {
        this.testScenarios = new ArrayList<>();
    }
    
    public FeatureFile(String fileName, TestSuiteType suiteType, String content, ApiSpec apiSpec) {
        this.fileName = fileName;
        this.suiteType = suiteType;
        this.content = content;
        this.apiSpec = apiSpec;
        this.generatedAt = LocalDateTime.now();
        this.testScenarios = new ArrayList<>();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public TestSuiteType getSuiteType() {
        return suiteType;
    }
    
    public void setSuiteType(TestSuiteType suiteType) {
        this.suiteType = suiteType;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public ApiSpec getApiSpec() {
        return apiSpec;
    }
    
    public void setApiSpec(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }
    
    public List<TestScenario> getTestScenarios() {
        return testScenarios;
    }
    
    public void setTestScenarios(List<TestScenario> testScenarios) {
        this.testScenarios = testScenarios;
    }
}
