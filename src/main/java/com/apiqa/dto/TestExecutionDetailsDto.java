package com.apiqa.dto;

import com.apiqa.model.TestExecutionStatus;
import java.time.LocalDateTime;

public class TestExecutionDetailsDto {
    private Long id;
    private TestExecutionStatus status;
    private LocalDateTime executedAt;
    private Long executionTimeMs;
    private Integer actualStatusCode;
    private String actualResponseBody;
    private String actualHeaders;
    private String errorMessage;
    private String validationResults;
    private String requestUrl;
    private String requestMethod;
    private String requestHeaders;
    private String requestBody;
    private Long testRunId;
    private Long testScenarioId;

    // Constructors
    public TestExecutionDetailsDto() {}

    public TestExecutionDetailsDto(Long id, TestExecutionStatus status, LocalDateTime executedAt, 
                                 Long executionTimeMs, Integer actualStatusCode, String actualResponseBody, 
                                 String actualHeaders, String errorMessage, String validationResults,
                                 String requestUrl, String requestMethod, String requestHeaders, 
                                 String requestBody, Long testRunId, Long testScenarioId) {
        this.id = id;
        this.status = status;
        this.executedAt = executedAt;
        this.executionTimeMs = executionTimeMs;
        this.actualStatusCode = actualStatusCode;
        this.actualResponseBody = actualResponseBody;
        this.actualHeaders = actualHeaders;
        this.errorMessage = errorMessage;
        this.validationResults = validationResults;
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.testRunId = testRunId;
        this.testScenarioId = testScenarioId;
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

    public Long getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(Long testRunId) {
        this.testRunId = testRunId;
    }

    public Long getTestScenarioId() {
        return testScenarioId;
    }

    public void setTestScenarioId(Long testScenarioId) {
        this.testScenarioId = testScenarioId;
    }
}
