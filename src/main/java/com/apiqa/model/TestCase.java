package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_cases")
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
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
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_spec_id")
    private ApiSpec apiSpec;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id", nullable = false)
    private TestSuite testSuite;
    
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestCaseStep> testCaseSteps;
    
    @OneToMany(mappedBy = "testCase", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestExecution> testExecutions;
    
    // Constructors
    public TestCase() {
        this.testCaseSteps = new ArrayList<>();
        this.testExecutions = new ArrayList<>();
    }
    
    public TestCase(String name, String description, String httpMethod, String endpoint, 
                   String requestBody, String expectedResponseSchema, String expectedHeaders, 
                   Integer expectedStatusCode, String createdBy, ApiSpec apiSpec, TestSuite testSuite) {
        this.name = name;
        this.description = description;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.requestBody = requestBody;
        this.expectedResponseSchema = expectedResponseSchema;
        this.expectedHeaders = expectedHeaders;
        this.expectedStatusCode = expectedStatusCode;
        this.createdBy = createdBy;
        this.apiSpec = apiSpec;
        this.testSuite = testSuite;
        this.createdAt = LocalDateTime.now();
        this.testCaseSteps = new ArrayList<>();
        this.testExecutions = new ArrayList<>();
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
    
    public ApiSpec getApiSpec() {
        return apiSpec;
    }
    
    public void setApiSpec(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }
    
    public TestSuite getTestSuite() {
        return testSuite;
    }
    
    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }
    
    public List<TestCaseStep> getTestCaseSteps() {
        return testCaseSteps;
    }
    
    public void setTestCaseSteps(List<TestCaseStep> testCaseSteps) {
        this.testCaseSteps = testCaseSteps;
    }
    
    public List<TestExecution> getTestExecutions() {
        return testExecutions;
    }
    
    public void setTestExecutions(List<TestExecution> testExecutions) {
        this.testExecutions = testExecutions;
    }
}
