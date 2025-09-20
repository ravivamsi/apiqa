package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_executions")
public class TestExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestExecutionStatus status;
    
    @Column
    private LocalDateTime executedAt;
    
    @Column
    private Long executionTimeMs;
    
    @Column
    private Integer actualStatusCode;
    
    @Column(columnDefinition = "TEXT")
    private String actualResponseBody;
    
    @Column(columnDefinition = "TEXT")
    private String actualHeaders;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String validationResults;
    
    @Column(columnDefinition = "TEXT")
    private String requestUrl;
    
    @Column
    private String requestMethod;
    
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;
    
    @Column(columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "test_run_id")
    private Long testRunId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_scenario_id", nullable = false)
    private TestScenario testScenario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_run_id", nullable = true, insertable = false, updatable = false)
    private TestRun testRun;
    
    // Constructors
    public TestExecution() {}
    
    public TestExecution(TestScenario testScenario, TestRun testRun) {
        this.testScenario = testScenario;
        this.testRun = testRun;
        this.testRunId = testRun != null ? testRun.getId() : null;
        this.status = TestExecutionStatus.PENDING;
        this.executedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TestExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(TestExecutionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public Integer getActualStatusCode() {
        return actualStatusCode;
    }
    
    public void setActualStatusCode(Integer actualStatusCode) {
        this.actualStatusCode = actualStatusCode;
    }
    
    public String getActualResponseBody() {
        return actualResponseBody;
    }
    
    public void setActualResponseBody(String actualResponseBody) {
        this.actualResponseBody = actualResponseBody;
    }
    
    public String getActualHeaders() {
        return actualHeaders;
    }
    
    public void setActualHeaders(String actualHeaders) {
        this.actualHeaders = actualHeaders;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getValidationResults() {
        return validationResults;
    }
    
    public void setValidationResults(String validationResults) {
        this.validationResults = validationResults;
    }
    
    public TestScenario getTestScenario() {
        return testScenario;
    }
    
    public void setTestScenario(TestScenario testScenario) {
        this.testScenario = testScenario;
    }
    
    public TestRun getTestRun() {
        return testRun;
    }
    
    public void setTestRun(TestRun testRun) {
        this.testRun = testRun;
        this.testRunId = testRun != null ? testRun.getId() : null;
    }
    
    public Long getTestRunId() {
        return testRunId;
    }
    
    public void setTestRunId(Long testRunId) {
        this.testRunId = testRunId;
    }
    
    public String getRequestUrl() {
        return requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    
    public String getRequestHeaders() {
        return requestHeaders;
    }
    
    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }
}
