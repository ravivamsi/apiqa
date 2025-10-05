package com.apiqa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_runs")
public class TestRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String runName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestRunStatus status;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TestRunType runType;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column
    private Integer totalTests;
    
    @Column
    private Integer passedTests;
    
    @Column
    private Integer failedTests;
    
    @Column
    private Integer skippedTests;
    
    @Column(columnDefinition = "TEXT")
    private String reportPath;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_spec_id")
    @JsonIgnore
    private ApiSpec apiSpec;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    @JsonIgnore
    private TestSuite testSuite;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id")
    @JsonIgnore
    private Environment environment;
    
    @Column
    private String testType;
    
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TestExecution> testExecutions;
    
    // Constructors
    public TestRun() {
        this.testExecutions = new java.util.ArrayList<>();
    }
    
    public TestRun(String runName, TestRunType runType, ApiSpec apiSpec) {
        this.runName = runName;
        this.runType = runType;
        this.apiSpec = apiSpec;
        this.status = TestRunStatus.PENDING;
        this.startedAt = LocalDateTime.now();
        this.testExecutions = new java.util.ArrayList<>();
    }
    
    public TestRun(String runName, TestRunType runType, String testType, TestSuite testSuite) {
        this.runName = runName;
        this.runType = runType;
        this.testType = testType;
        this.testSuite = testSuite;
        this.status = TestRunStatus.PENDING;
        this.startedAt = LocalDateTime.now();
        this.testExecutions = new java.util.ArrayList<>();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRunName() {
        return runName;
    }
    
    public void setRunName(String runName) {
        this.runName = runName;
    }
    
    public TestRunStatus getStatus() {
        return status;
    }
    
    public void setStatus(TestRunStatus status) {
        this.status = status;
    }
    
    public TestRunType getRunType() {
        return runType;
    }
    
    public void setRunType(TestRunType runType) {
        this.runType = runType;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getTotalTests() {
        return totalTests;
    }
    
    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }
    
    public Integer getPassedTests() {
        return passedTests;
    }
    
    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }
    
    public Integer getFailedTests() {
        return failedTests;
    }
    
    public void setFailedTests(Integer failedTests) {
        this.failedTests = failedTests;
    }
    
    public Integer getSkippedTests() {
        return skippedTests;
    }
    
    public void setSkippedTests(Integer skippedTests) {
        this.skippedTests = skippedTests;
    }
    
    public String getReportPath() {
        return reportPath;
    }
    
    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public ApiSpec getApiSpec() {
        return apiSpec;
    }
    
    public void setApiSpec(ApiSpec apiSpec) {
        this.apiSpec = apiSpec;
    }
    
    public List<TestExecution> getTestExecutions() {
        return testExecutions;
    }
    
    public void setTestExecutions(List<TestExecution> testExecutions) {
        this.testExecutions = testExecutions;
    }
    
    public TestSuite getTestSuite() {
        return testSuite;
    }
    
    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public String getTestType() {
        return testType;
    }
    
    public void setTestType(String testType) {
        this.testType = testType;
    }
    
    public LocalDateTime getEndedAt() {
        return completedAt;
    }
    
    public void setEndedAt(LocalDateTime endedAt) {
        this.completedAt = endedAt;
    }
}
