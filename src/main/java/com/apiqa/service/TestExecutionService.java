package com.apiqa.service;

import com.apiqa.model.*;
import com.apiqa.repository.TestExecutionRepository;
import com.apiqa.repository.TestRunRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TestExecutionService {
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private TestRunRepository testRunRepository;
    
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Transactional
    public TestExecution saveTestExecution(TestExecution execution) {
        return testExecutionRepository.save(execution);
    }
    
    public Optional<TestExecution> getTestExecutionById(Long id) {
        return testExecutionRepository.findById(id);
    }
    
    public TestRun executeTestRun(TestRun testRun, List<TestExecution> executions) {
        testRun.setStatus(TestRunStatus.RUNNING);
        testRun.setStartedAt(LocalDateTime.now());
        testRunRepository.save(testRun);
        
        try {
            // Execute tests in parallel without saving to database initially
            List<CompletableFuture<TestExecution>> futures = executions.stream()
                    .map(execution -> CompletableFuture.supplyAsync(() -> executeTest(execution), executorService))
                    .toList();
            
            // Wait for all tests to complete and collect results
            List<TestExecution> completedExecutions = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
            
            // Save completed executions to database
            for (TestExecution execution : completedExecutions) {
                try {
                    testExecutionRepository.save(execution);
                } catch (Exception e) {
                    // Log error but continue with other executions
                    System.err.println("Failed to save execution: " + e.getMessage());
                }
            }
            
            // Update test run statistics
            updateTestRunStatistics(testRun);
            
            testRun.setStatus(TestRunStatus.COMPLETED);
            testRun.setCompletedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            testRun.setStatus(TestRunStatus.FAILED);
            testRun.setErrorMessage(e.getMessage());
            testRun.setCompletedAt(LocalDateTime.now());
        }
        
        return testRunRepository.save(testRun);
    }
    
    public TestRun executeTestRun(TestRun testRun) {
        testRun.setStatus(TestRunStatus.RUNNING);
        testRun.setStartedAt(LocalDateTime.now());
        testRunRepository.save(testRun);
        
        try {
            // Save all test executions first
            List<TestExecution> executions = new ArrayList<>();
            for (TestExecution execution : testRun.getTestExecutions()) {
                execution = testExecutionRepository.save(execution);
                executions.add(execution);
            }
            
            // Execute tests in parallel
            List<CompletableFuture<Void>> futures = executions.stream()
                    .map(execution -> CompletableFuture.runAsync(() -> executeTest(execution), executorService))
                    .toList();
            
            // Wait for all tests to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // Update test run statistics
            updateTestRunStatistics(testRun);
            
            testRun.setStatus(TestRunStatus.COMPLETED);
            testRun.setCompletedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            testRun.setStatus(TestRunStatus.FAILED);
            testRun.setErrorMessage(e.getMessage());
            testRun.setCompletedAt(LocalDateTime.now());
        }
        
        return testRunRepository.save(testRun);
    }
    
    public TestExecution executeTest(TestExecution execution) {
        execution.setStatus(TestExecutionStatus.RUNNING);
        execution.setExecutedAt(LocalDateTime.now());
        // Don't save to database initially to avoid constraint issues
        
        long startTime = System.currentTimeMillis();
        
        try {
            TestScenario scenario = execution.getTestScenario();
            
            // Store request details
            execution.setRequestUrl(scenario.getEndpoint());
            execution.setRequestMethod(scenario.getHttpMethod());
            execution.setRequestBody(scenario.getRequestBody());
            
            // Build request headers
            StringBuilder requestHeaders = new StringBuilder();
            requestHeaders.append("Content-Type: application/json\n");
            requestHeaders.append("Accept: application/json\n");
            requestHeaders.append("User-Agent: APIQA-TestRunner/1.0");
            execution.setRequestHeaders(requestHeaders.toString());
            
            // Make HTTP request
            WebClient.RequestBodySpec requestSpec = webClient
                    .method(HttpMethod.valueOf(scenario.getHttpMethod()))
                    .uri(scenario.getEndpoint());
            
            WebClient.ResponseSpec responseSpec;
            if (scenario.getRequestBody() != null && !scenario.getRequestBody().isEmpty()) {
                responseSpec = requestSpec
                        .header("Content-Type", "application/json")
                        .bodyValue(scenario.getRequestBody())
                        .retrieve();
            } else {
                responseSpec = requestSpec.retrieve();
            }
            
            // Capture status code and body
            Mono<ResponseEntity<String>> responseMono = responseSpec
                    .toEntity(String.class)
                    .timeout(Duration.ofSeconds(30));
            
            ResponseEntity<String> responseEntity = responseMono.block();
            
            String responseBody = responseEntity.getBody();
            Integer statusCode = responseEntity.getStatusCode().value();
            
            // Store response headers
            StringBuilder responseHeaders = new StringBuilder();
            responseEntity.getHeaders().forEach((key, values) -> {
                responseHeaders.append(key).append(": ").append(String.join(", ", values)).append("\n");
            });
            execution.setActualHeaders(responseHeaders.toString());
            
            long executionTime = System.currentTimeMillis() - startTime;
            execution.setExecutionTimeMs(executionTime);
            execution.setActualResponseBody(responseBody);
            execution.setActualStatusCode(statusCode);
            
            // Validate response
            boolean isValid = validateResponse(execution);
            execution.setStatus(isValid ? TestExecutionStatus.PASSED : TestExecutionStatus.FAILED);
            
            if (!isValid) {
                execution.setErrorMessage("Response validation failed");
            }
            
        } catch (Exception e) {
            execution.setStatus(TestExecutionStatus.ERROR);
            execution.setErrorMessage(e.getMessage());
            long executionTime = System.currentTimeMillis() - startTime;
            execution.setExecutionTimeMs(executionTime);
        }
        
        // Don't save to database initially to avoid constraint issues
        return execution;
    }
    
    private boolean validateResponse(TestExecution execution) {
        try {
            TestScenario scenario = execution.getTestScenario();
            
            // Validate status code
            if (scenario.getExpectedStatusCode() != null && 
                !scenario.getExpectedStatusCode().equals(execution.getActualStatusCode())) {
                return false;
            }
            
            // Validate response body schema
            if (scenario.getExpectedResponseSchema() != null && execution.getActualResponseBody() != null) {
                if (!validateJsonSchema(execution.getActualResponseBody(), scenario.getExpectedResponseSchema())) {
                    return false;
                }
            }
            
            // Validate response headers
            if (scenario.getExpectedHeaders() != null && execution.getActualHeaders() != null) {
                if (!validateHeaders(execution.getActualHeaders(), scenario.getExpectedHeaders())) {
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            execution.setErrorMessage("Validation error: " + e.getMessage());
            return false;
        }
    }
    
    private boolean validateJsonSchema(String responseBody, String expectedSchema) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            // Basic JSON validation - in a real implementation, you'd use a proper JSON schema validator
            return responseJson != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateHeaders(String actualHeaders, String expectedHeaders) {
        // Basic header validation - in a real implementation, you'd parse and compare headers properly
        return actualHeaders != null && !actualHeaders.isEmpty();
    }
    
    private void updateTestRunStatistics(TestRun testRun) {
        List<TestExecution> executions = testExecutionRepository.findByTestRunId(testRun.getId());
        
        int totalTests = executions.size();
        int passedTests = (int) executions.stream().filter(e -> e.getStatus() == TestExecutionStatus.PASSED).count();
        int failedTests = (int) executions.stream().filter(e -> e.getStatus() == TestExecutionStatus.FAILED).count();
        int skippedTests = (int) executions.stream().filter(e -> e.getStatus() == TestExecutionStatus.SKIPPED).count();
        
        testRun.setTotalTests(totalTests);
        testRun.setPassedTests(passedTests);
        testRun.setFailedTests(failedTests);
        testRun.setSkippedTests(skippedTests);
    }
    
    public TestRun retryFailedTests(TestRun originalRun) {
        TestRun retryRun = new TestRun(
                "Retry - " + originalRun.getRunName(),
                TestRunType.RETRY,
                originalRun.getApiSpec()
        );
        retryRun = testRunRepository.save(retryRun);
        
        // Find failed test executions from the original run
        List<TestExecution> failedExecutions = testExecutionRepository
                .findByTestRunIdAndStatus(originalRun.getId(), TestExecutionStatus.FAILED);
        
        // Create new test executions for retry
        for (TestExecution failedExecution : failedExecutions) {
            TestExecution retryExecution = new TestExecution(failedExecution.getTestScenario(), retryRun);
            testExecutionRepository.save(retryExecution);
        }
        
        return executeTestRun(retryRun);
    }
}
