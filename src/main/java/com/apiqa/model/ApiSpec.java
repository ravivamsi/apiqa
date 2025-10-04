package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "api_specs")
public class ApiSpec {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String openApiYaml;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt;
    
    @OneToMany(mappedBy = "apiSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeatureFile> featureFiles;
    
    @OneToMany(mappedBy = "apiSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestRun> testRuns;
    
    // Constructors
    public ApiSpec() {
        this.featureFiles = new ArrayList<>();
        this.testRuns = new ArrayList<>();
    }
    
    public ApiSpec(String name, String openApiYaml, String version) {
        this.name = name;
        this.openApiYaml = openApiYaml;
        this.version = version;
        this.uploadedAt = LocalDateTime.now();
        this.featureFiles = new ArrayList<>();
        this.testRuns = new ArrayList<>();
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
    
    public String getOpenApiYaml() {
        return openApiYaml;
    }
    
    public void setOpenApiYaml(String openApiYaml) {
        this.openApiYaml = openApiYaml;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
    
    public List<FeatureFile> getFeatureFiles() {
        return featureFiles;
    }
    
    public void setFeatureFiles(List<FeatureFile> featureFiles) {
        this.featureFiles = featureFiles;
    }
    
    public List<TestRun> getTestRuns() {
        return testRuns;
    }
    
    public void setTestRuns(List<TestRun> testRuns) {
        this.testRuns = testRuns;
    }
}
