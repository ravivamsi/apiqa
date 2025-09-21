package com.apiqa.service;

import com.apiqa.model.*;
import com.apiqa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private CustomEndpointRepository customEndpointRepository;
    
    @Autowired
    private TestSuiteRepository testSuiteRepository;
    
    @Autowired
    private TestCaseStepRepository testCaseStepRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private ApiSpecRepository apiSpecRepository;
    
    @Autowired
    private TestRunRepository testRunRepository;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    // Test Suite Management
    public TestSuite createTestSuite(String name, String description, com.apiqa.model.TestType testType, String createdBy) {
        TestSuite testSuite = new TestSuite(name, description, testType, createdBy);
        return testSuiteRepository.save(testSuite);
    }
    
    public List<TestSuite> getAllTestSuites() {
        return testSuiteRepository.findAll();
    }
    
    public Optional<TestSuite> getTestSuiteById(Long id) {
        return testSuiteRepository.findById(id);
    }
    
    public TestSuite updateTestSuite(Long id, String name, String description, com.apiqa.model.TestType testType) {
        Optional<TestSuite> testSuiteOpt = testSuiteRepository.findById(id);
        if (testSuiteOpt.isPresent()) {
            TestSuite testSuite = testSuiteOpt.get();
            testSuite.setName(name);
            testSuite.setDescription(description);
            testSuite.setTestType(testType);
            return testSuiteRepository.save(testSuite);
        }
        throw new RuntimeException("Test Suite not found with id: " + id);
    }
    
    public void deleteTestSuite(Long id) {
        testSuiteRepository.deleteById(id);
    }
    
    // Test Case Management
    public TestCase createTestCase(String name, String description, String httpMethod, String endpoint,
                                  String requestBody, String expectedResponseSchema, String expectedHeaders,
                                  Integer expectedStatusCode, String createdBy, Long apiSpecId, Long testSuiteId) {
        ApiSpec apiSpec = null;
        if (apiSpecId != null) {
            apiSpec = apiSpecRepository.findById(apiSpecId).orElse(null);
        }
        
        TestSuite testSuite = null;
        if (testSuiteId != null) {
            testSuite = testSuiteRepository.findById(testSuiteId).orElse(null);
        }
        
        TestCase testCase = new TestCase(name, description, httpMethod, endpoint,
                requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode,
                createdBy, apiSpec, testSuite);
        return testCaseRepository.save(testCase);
    }
    
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }
    
    public List<TestCase> getTestCasesByTestSuite(Long testSuiteId) {
        return testCaseRepository.findByTestSuiteId(testSuiteId);
    }
    
    public List<TestCase> getTestCasesByApiSpec(Long apiSpecId) {
        return testCaseRepository.findByApiSpecId(apiSpecId);
    }
    
    public Optional<TestCase> getTestCaseById(Long id) {
        return testCaseRepository.findById(id);
    }
    
    public TestCase updateTestCase(Long id, String name, String description, String httpMethod,
                                  String endpoint, String requestBody, String expectedResponseSchema,
                                  String expectedHeaders, Integer expectedStatusCode, Long apiSpecId, Long testSuiteId) {
        Optional<TestCase> testCaseOpt = testCaseRepository.findById(id);
        if (testCaseOpt.isPresent()) {
            TestCase testCase = testCaseOpt.get();
            testCase.setName(name);
            testCase.setDescription(description);
            testCase.setHttpMethod(httpMethod);
            testCase.setEndpoint(endpoint);
            testCase.setRequestBody(requestBody);
            testCase.setExpectedResponseSchema(expectedResponseSchema);
            testCase.setExpectedHeaders(expectedHeaders);
            testCase.setExpectedStatusCode(expectedStatusCode);
            
            if (apiSpecId != null) {
                ApiSpec apiSpec = apiSpecRepository.findById(apiSpecId).orElse(null);
                testCase.setApiSpec(apiSpec);
            }
            
            if (testSuiteId != null) {
                TestSuite testSuite = testSuiteRepository.findById(testSuiteId).orElse(null);
                testCase.setTestSuite(testSuite);
            }
            
            return testCaseRepository.save(testCase);
        }
        throw new RuntimeException("Test Case not found with id: " + id);
    }
    
    public void deleteTestCase(Long id) {
        testCaseRepository.deleteById(id);
    }
    
    // Custom Endpoint Management
    public CustomEndpoint createCustomEndpoint(String name, String description, String httpMethod, 
                                             String endpoint, String requestBody, String expectedResponseSchema,
                                             String expectedHeaders, Integer expectedStatusCode, String createdBy,
                                             Long apiSpecId, Long testSuiteId) {
        ApiSpec apiSpec = null;
        if (apiSpecId != null) {
            apiSpec = apiSpecRepository.findById(apiSpecId).orElse(null);
        }
        
        TestSuite testSuite = null;
        if (testSuiteId != null) {
            testSuite = testSuiteRepository.findById(testSuiteId).orElse(null);
        }
        
        CustomEndpoint customEndpoint = new CustomEndpoint(name, description, httpMethod, endpoint,
                requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode, createdBy, apiSpec, testSuite);
        return customEndpointRepository.save(customEndpoint);
    }
    
    public List<CustomEndpoint> getAllCustomEndpoints() {
        return customEndpointRepository.findAll();
    }
    
    public List<CustomEndpoint> getCustomEndpointsByTestSuite(Long testSuiteId) {
        return customEndpointRepository.findByTestSuiteId(testSuiteId);
    }
    
    public List<CustomEndpoint> getCustomEndpointsByApiSpec(Long apiSpecId) {
        return customEndpointRepository.findByApiSpecId(apiSpecId);
    }
    
    public Optional<CustomEndpoint> getCustomEndpointById(Long id) {
        return customEndpointRepository.findById(id);
    }
    
    public CustomEndpoint updateCustomEndpoint(Long id, String name, String description, String httpMethod,
                                             String endpoint, String requestBody, String expectedResponseSchema,
                                             String expectedHeaders, Integer expectedStatusCode, Long apiSpecId, Long testSuiteId) {
        Optional<CustomEndpoint> endpointOpt = customEndpointRepository.findById(id);
        if (endpointOpt.isPresent()) {
            CustomEndpoint customEndpoint = endpointOpt.get();
            customEndpoint.setName(name);
            customEndpoint.setDescription(description);
            customEndpoint.setHttpMethod(httpMethod);
            customEndpoint.setEndpoint(endpoint);
            customEndpoint.setRequestBody(requestBody);
            customEndpoint.setExpectedResponseSchema(expectedResponseSchema);
            customEndpoint.setExpectedHeaders(expectedHeaders);
            customEndpoint.setExpectedStatusCode(expectedStatusCode);
            
            if (apiSpecId != null) {
                ApiSpec apiSpec = apiSpecRepository.findById(apiSpecId).orElse(null);
                customEndpoint.setApiSpec(apiSpec);
            }
            
            if (testSuiteId != null) {
                TestSuite testSuite = testSuiteRepository.findById(testSuiteId).orElse(null);
                customEndpoint.setTestSuite(testSuite);
            }
            
            return customEndpointRepository.save(customEndpoint);
        }
        throw new RuntimeException("Custom Endpoint not found with id: " + id);
    }
    
    public void deleteCustomEndpoint(Long id) {
        customEndpointRepository.deleteById(id);
    }
    
    // Test Case Step Management
    public TestCaseStep createTestCaseStep(String stepName, String description, String stepType,
                                          String expectedValue, String jsonPath, String assertionType,
                                          String assertionValue, Integer stepOrder, String createdBy,
                                          Long testCaseId) {
        TestCase testCase = null;
        if (testCaseId != null) {
            testCase = testCaseRepository.findById(testCaseId).orElse(null);
        }
        
        TestCaseStep testCaseStep = new TestCaseStep(stepName, description, stepType, expectedValue,
                jsonPath, assertionType, assertionValue, stepOrder, createdBy, testCase);
        return testCaseStepRepository.save(testCaseStep);
    }
    
    public List<TestCaseStep> getTestCaseStepsByTestCase(Long testCaseId) {
        return testCaseStepRepository.findByTestCaseIdOrderByStepOrder(testCaseId);
    }
    
    public Optional<TestCaseStep> getTestCaseStepById(Long id) {
        return testCaseStepRepository.findById(id);
    }
    
    public TestCaseStep updateTestCaseStep(Long id, String stepName, String description, String stepType,
                                          String expectedValue, String jsonPath, String assertionType,
                                          String assertionValue, Integer stepOrder, Long testCaseId) {
        Optional<TestCaseStep> stepOpt = testCaseStepRepository.findById(id);
        if (stepOpt.isPresent()) {
            TestCaseStep testCaseStep = stepOpt.get();
            testCaseStep.setStepName(stepName);
            testCaseStep.setDescription(description);
            testCaseStep.setStepType(stepType);
            testCaseStep.setExpectedValue(expectedValue);
            testCaseStep.setJsonPath(jsonPath);
            testCaseStep.setAssertionType(assertionType);
            testCaseStep.setAssertionValue(assertionValue);
            testCaseStep.setStepOrder(stepOrder);
            
            if (testCaseId != null) {
                TestCase testCase = testCaseRepository.findById(testCaseId).orElse(null);
                testCaseStep.setTestCase(testCase);
            }
            
            return testCaseStepRepository.save(testCaseStep);
        }
        throw new RuntimeException("Test Case Step not found with id: " + id);
    }
    
    public void deleteTestCaseStep(Long id) {
        testCaseStepRepository.deleteById(id);
    }
    
    public void reorderTestCaseSteps(Long testCaseId, List<Long> stepIds) {
        List<TestCaseStep> steps = testCaseStepRepository.findByTestCaseIdOrderByStepOrder(testCaseId);
        for (int i = 0; i < stepIds.size(); i++) {
            Long stepId = stepIds.get(i);
            TestCaseStep step = steps.stream()
                    .filter(s -> s.getId().equals(stepId))
                    .findFirst()
                    .orElse(null);
            if (step != null) {
                step.setStepOrder(i + 1);
                testCaseStepRepository.save(step);
            }
        }
    }
    
    // Execution Methods
    public com.apiqa.model.TestRun executeTestSuite(Long testSuiteId, String runName) {
        return testExecutionService.executeTestSuite(testSuiteId, runName);
    }
    
    public com.apiqa.model.TestExecution executeTestCase(Long testCaseId) {
        Optional<TestCase> testCaseOpt = testCaseRepository.findById(testCaseId);
        if (testCaseOpt.isEmpty()) {
            throw new RuntimeException("Test Case not found with id: " + testCaseId);
        }
        
        // Create a temporary test run for single test case execution
        TestCase testCase = testCaseOpt.get();
        TestRun testRun = new TestRun(
            "Single Test Case Execution", 
            TestRunType.MANUAL, 
            testCase.getTestSuite().getTestType().name(), 
            testCase.getTestSuite()
        );
        testRun = testRunRepository.save(testRun);
        
        return testExecutionService.executeTestCase(testCase, testRun);
    }
    
    public boolean executeTestCaseStep(Long testCaseStepId) {
        Optional<TestCaseStep> stepOpt = testCaseStepRepository.findById(testCaseStepId);
        if (stepOpt.isEmpty()) {
            throw new RuntimeException("Test Case Step not found with id: " + testCaseStepId);
        }
        
        TestCaseStep step = stepOpt.get();
        TestCase testCase = step.getTestCase();
        
        // Execute the test case to get the response, then execute just this step
        TestRun testRun = new TestRun(
            "Single Step Execution", 
            TestRunType.MANUAL, 
            testCase.getTestSuite().getTestType().name(), 
            testCase.getTestSuite()
        );
        testRun = testRunRepository.save(testRun);
        
        TestExecution execution = testExecutionService.executeTestCase(testCase, testRun);
        
        // Extract the response from the execution and execute just this step
        try {
            org.springframework.http.ResponseEntity<String> response = new org.springframework.http.ResponseEntity<>(
                execution.getActualResponseBody(),
                org.springframework.http.HttpStatus.valueOf(execution.getActualStatusCode())
            );
            
            java.util.List<String> validationResults = new java.util.ArrayList<>();
            return testExecutionService.executeTestCaseStep(step, response, validationResults);
        } catch (Exception e) {
            return false;
        }
    }
}
