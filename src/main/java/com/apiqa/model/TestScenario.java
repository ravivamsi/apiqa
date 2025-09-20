package com.apiqa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_scenarios")
public class TestScenario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String scenarioName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String httpMethod;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(columnDefinition = "TEXT")
    private String expectedResponseSchema;
    
    @Column(columnDefinition = "TEXT")
    private String expectedHeaders;
    
    @Column
    private Integer expectedStatusCode;
    
    @Column(columnDefinition = "TEXT")
    private String testSteps;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_file_id", nullable = false)
    @JsonIgnore
    private FeatureFile featureFile;
    
    @OneToMany(mappedBy = "testScenario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestExecution> testExecutions;
    
    // Constructors
    public TestScenario() {}
    
    public TestScenario(String scenarioName, String description, String httpMethod, String endpoint, 
                       String requestBody, String expectedResponseSchema, String expectedHeaders, 
                       Integer expectedStatusCode, String testSteps, FeatureFile featureFile) {
        this.scenarioName = scenarioName;
        this.description = description;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.requestBody = requestBody;
        this.expectedResponseSchema = expectedResponseSchema;
        this.expectedHeaders = expectedHeaders;
        this.expectedStatusCode = expectedStatusCode;
        this.testSteps = testSteps;
        this.featureFile = featureFile;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getScenarioName() {
        return scenarioName;
    }
    
    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
    
    public String getExpectedResponseSchema() {
        return expectedResponseSchema;
    }
    
    public void setExpectedResponseSchema(String expectedResponseSchema) {
        this.expectedResponseSchema = expectedResponseSchema;
    }
    
    public String getExpectedHeaders() {
        return expectedHeaders;
    }
    
    public void setExpectedHeaders(String expectedHeaders) {
        this.expectedHeaders = expectedHeaders;
    }
    
    public Integer getExpectedStatusCode() {
        return expectedStatusCode;
    }
    
    public void setExpectedStatusCode(Integer expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }
    
    public String getTestSteps() {
        return testSteps;
    }
    
    public void setTestSteps(String testSteps) {
        this.testSteps = testSteps;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public FeatureFile getFeatureFile() {
        return featureFile;
    }
    
    public void setFeatureFile(FeatureFile featureFile) {
        this.featureFile = featureFile;
    }
    
    public List<TestExecution> getTestExecutions() {
        return testExecutions;
    }
    
    public void setTestExecutions(List<TestExecution> testExecutions) {
        this.testExecutions = testExecutions;
    }
}
