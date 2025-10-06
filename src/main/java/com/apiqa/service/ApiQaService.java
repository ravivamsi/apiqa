package com.apiqa.service;

import com.apiqa.model.*;
import com.apiqa.repository.ApiSpecRepository;
import com.apiqa.repository.FeatureFileRepository;
import com.apiqa.repository.TestRunRepository;
import com.apiqa.repository.TestScenarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class ApiQaService {
    
    @Autowired
    private ApiSpecRepository apiSpecRepository;
    
    @Autowired
    private TestRunRepository testRunRepository;
    
    @Autowired
    private TestScenarioRepository testScenarioRepository;
    
    @Autowired
    private FeatureFileRepository featureFileRepository;
    
    @Autowired
    private OpenApiParserService parserService;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    public ApiSpec uploadApiSpec(String name, String openApiYaml) {
        // Extract version from OpenAPI YAML
        String version = extractVersionFromYaml(openApiYaml);
        ApiSpec apiSpec = new ApiSpec(name, openApiYaml, version);
        return apiSpecRepository.save(apiSpec);
    }
    
    private String extractVersionFromYaml(String openApiYaml) {
        try {
            // Parse the YAML to extract version
            ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
            JsonNode rootNode = yamlMapper.readTree(openApiYaml);
            
            // Try to get version from info.version
            JsonNode infoNode = rootNode.get("info");
            if (infoNode != null && infoNode.has("version")) {
                return infoNode.get("version").asText();
            }
            
            // Fallback to default version if not found
            return "1.0.0";
        } catch (Exception e) {
            System.err.println("Failed to extract version from YAML: " + e.getMessage());
            return "1.0.0"; // Default fallback
        }
    }
    
    public List<FeatureFile> generateFeatureFiles(Long apiSpecId) {
        Optional<ApiSpec> apiSpecOpt = apiSpecRepository.findById(apiSpecId);
        if (apiSpecOpt.isEmpty()) {
            throw new RuntimeException("API Spec not found with ID: " + apiSpecId);
        }
        
        ApiSpec apiSpec = apiSpecOpt.get();
        
        // Delete existing feature files and their test scenarios
        System.out.println("Deleting existing tests for API Spec ID: " + apiSpecId);
        for (FeatureFile existingFeatureFile : apiSpec.getFeatureFiles()) {
            // Delete test scenarios first (due to foreign key constraints)
            for (TestScenario scenario : existingFeatureFile.getTestScenarios()) {
                testScenarioRepository.delete(scenario);
            }
            // Delete the feature file
            featureFileRepository.delete(existingFeatureFile);
        }
        
        // Clear the feature files list
        apiSpec.getFeatureFiles().clear();
        apiSpecRepository.save(apiSpec);
        
        System.out.println("Existing tests deleted. Generating new tests...");
        
        try {
            // Generate new feature files
            List<FeatureFile> featureFiles = parserService.parseOpenApiSpec(apiSpec.getOpenApiYaml(), apiSpec);
            System.out.println("Generated " + featureFiles.size() + " feature files");
            
            // Save each feature file with its test scenarios
            for (FeatureFile featureFile : featureFiles) {
                System.out.println("Saving feature file: " + featureFile.getFileName());
                // Save the feature file first to get an ID
                apiSpec.getFeatureFiles().add(featureFile);
                apiSpecRepository.save(apiSpec);
                
                // Now save test scenarios with the proper feature file reference
                for (TestScenario scenario : featureFile.getTestScenarios()) {
                    scenario.setFeatureFile(featureFile);
                    testScenarioRepository.save(scenario);
                }
                System.out.println("Saved " + featureFile.getTestScenarios().size() + " test scenarios for " + featureFile.getFileName());
            }
            
            System.out.println("New tests generated successfully. Total feature files: " + featureFiles.size());
            return featureFiles;
        } catch (Exception e) {
            System.err.println("Error generating feature files: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate tests: " + e.getMessage(), e);
        }
    }
    
    public TestRun executeTestRun(Long apiSpecId, String runName, TestRunType runType) {
        Optional<ApiSpec> apiSpecOpt = apiSpecRepository.findById(apiSpecId);
        if (apiSpecOpt.isEmpty()) {
            throw new RuntimeException("API Spec not found with ID: " + apiSpecId);
        }
        
        ApiSpec apiSpec = apiSpecOpt.get();
        
        // Create and save test run first
        TestRun testRun = new TestRun(runName, runType, apiSpec);
        testRun = testRunRepository.save(testRun);
        
        // Generate test executions for all scenarios (don't save yet)
        List<TestExecution> executions = new ArrayList<>();
        for (FeatureFile featureFile : apiSpec.getFeatureFiles()) {
            for (TestScenario scenario : featureFile.getTestScenarios()) {
                TestExecution execution = new TestExecution(scenario, testRun);
                executions.add(execution);
            }
        }
        
        // Add executions to test run (but don't save yet)
        testRun.getTestExecutions().addAll(executions);
        
        // Execute tests synchronously - this will handle saving the executions
        TestRun result = testExecutionService.executeTestRun(testRun, executions);
        
        return result;
    }
    
    public TestRun executeTestRunBySuiteType(Long apiSpecId, String runName, TestRunType runType, TestSuiteType suiteType) {
        Optional<ApiSpec> apiSpecOpt = apiSpecRepository.findById(apiSpecId);
        if (apiSpecOpt.isEmpty()) {
            throw new RuntimeException("API Spec not found with ID: " + apiSpecId);
        }
        
        ApiSpec apiSpec = apiSpecOpt.get();
        
        // Create and save test run first
        TestRun testRun = new TestRun(runName, runType, apiSpec);
        testRun = testRunRepository.save(testRun);
        
        // Generate test executions only for scenarios in the specified suite type
        List<TestExecution> executions = new ArrayList<>();
        for (FeatureFile featureFile : apiSpec.getFeatureFiles()) {
            if (featureFile.getSuiteType() == suiteType) {
                for (TestScenario scenario : featureFile.getTestScenarios()) {
                    TestExecution execution = new TestExecution(scenario, testRun);
                    executions.add(execution);
                }
            }
        }
        
        if (executions.isEmpty()) {
            throw new RuntimeException("No test scenarios found for suite type: " + suiteType);
        }
        
        // Add executions to test run (but don't save yet)
        testRun.getTestExecutions().addAll(executions);
        
        // Execute tests synchronously - this will handle saving the executions
        TestRun result = testExecutionService.executeTestRun(testRun, executions);
        
        return result;
    }
    
    @Async
    public CompletableFuture<TestRun> executeTestRunAsync(TestRun testRun) {
        TestRun result = testExecutionService.executeTestRun(testRun);
        return CompletableFuture.completedFuture(result);
    }
    
    public TestRun retryFailedTests(Long testRunId) {
        Optional<TestRun> testRunOpt = testRunRepository.findById(testRunId);
        if (testRunOpt.isEmpty()) {
            throw new RuntimeException("Test Run not found with ID: " + testRunId);
        }
        
        return testExecutionService.retryFailedTests(testRunOpt.get());
    }
    
    public List<ApiSpec> getAllApiSpecs() {
        return apiSpecRepository.findAllWithFeatureFilesOrderByUploadedAtDesc();
    }
    
    public Optional<ApiSpec> getApiSpecById(Long id) {
        return apiSpecRepository.findById(id);
    }
    
    public List<TestRun> getTestRunsByApiSpecId(Long apiSpecId) {
        return testRunRepository.findByApiSpecIdOrderByStartedAtDesc(apiSpecId);
    }
    
    public List<TestRun> getAllTestRuns() {
        return testRunRepository.findAll();
    }
    
    public Optional<TestRun> getTestRunById(Long id) {
        return testRunRepository.findById(id);
    }
    
    public void deleteApiSpec(Long id) {
        apiSpecRepository.deleteById(id);
    }
    
    public void deleteTestRun(Long id) {
        testRunRepository.deleteById(id);
    }
    
    public Optional<FeatureFile> getFeatureFileById(Long id) {
        return featureFileRepository.findById(id);
    }
    
    public FeatureFile saveFeatureFile(FeatureFile featureFile) {
        return featureFileRepository.save(featureFile);
    }
}
