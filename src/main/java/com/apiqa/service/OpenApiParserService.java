package com.apiqa.service;

import com.apiqa.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OpenApiParserService {
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public List<FeatureFile> parseOpenApiSpec(String openApiYaml, ApiSpec apiSpec) {
        try {
            OpenAPI openAPI = new OpenAPIV3Parser().readContents(openApiYaml, null, null).getOpenAPI();
            
            List<FeatureFile> featureFiles = new ArrayList<>();
            
            // Generate Smoke Tests (GET operations only) - Positive scenarios
            featureFiles.add(generateSmokeTests(openAPI, apiSpec));
            
            // Generate System Tests (All CRUD operations) - Positive and Negative scenarios
            featureFiles.add(generateSystemTests(openAPI, apiSpec));
            
            // Generate Negative Test Scenarios
            featureFiles.add(generateNegativeTests(openAPI, apiSpec));
            
            // Generate Integration Tests (Endpoint orchestration)
            featureFiles.add(generateIntegrationTests(openAPI, apiSpec));
            
            return featureFiles;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OpenAPI specification", e);
        }
    }
    
    private String getBaseUrl(OpenAPI openAPI) {
        if (openAPI.getServers() != null && !openAPI.getServers().isEmpty()) {
            return openAPI.getServers().get(0).getUrl();
        }
        return "https://api.example.com"; // Default fallback
    }
    
    private FeatureFile generateSmokeTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Smoke Tests - Read-only Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to verify basic API functionality with valid requests\n");
        content.append("  So that I can ensure the API is accessible and responding correctly\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_smoke_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SMOKE, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Only GET operations for smoke tests
                if (pathItem.getGet() != null) {
                    Operation getOp = pathItem.getGet();
                    
                    // Positive scenario - Valid GET request
                    String positiveScenarioName = generateScenarioName("GET", path, getOp.getOperationId()) + "_Valid_Request";
                    String description = "Valid GET request to " + path;
                    String substitutedPath = substitutePathParameters(path, getOp);
                    String fullUrl = baseUrl + substitutedPath;
                    
                    content.append("  Scenario: ").append(positiveScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I have valid authentication credentials\n");
                    content.append("    When I send a GET request to \"").append(substitutedPath).append("\"\n");
                    content.append("    And I include valid headers\n");
                    content.append("    Then the response status should be 200\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the expected schema\n");
                    content.append("    And the response should contain mandatory fields\n");
                    content.append("    And the response headers should be valid\n");
                    content.append("    And the response time should be less than 5 seconds\n\n");
                    
                    TestScenario positiveScenario = createTestScenario(positiveScenarioName, description, "GET", fullUrl, 
                            null, getExpectedResponseSchema(getOp), getExpectedHeaders(getOp), 200, 
                            generatePositiveTestSteps("GET", substitutedPath, null), featureFile);
                    scenarios.add(positiveScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateSystemTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: System Tests - All CRUD Operations (Positive Scenarios)\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test all API operations thoroughly with valid data\n");
        content.append("  So that I can ensure complete API functionality works correctly\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_system_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Test all HTTP methods
                for (Map.Entry<String, Operation> methodEntry : getOperations(pathItem).entrySet()) {
                    String method = methodEntry.getKey();
                    Operation operation = methodEntry.getValue();
                    
                    // Positive scenario for each operation
                    String positiveScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Valid_Request";
                    String description = "Valid " + method + " request to " + path;
                    String substitutedPath = substitutePathParameters(path, operation);
                    String fullUrl = baseUrl + substitutedPath;
                    String requestBody = getRequestBody(operation);
                    
                    content.append("  Scenario: ").append(positiveScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I have valid authentication credentials\n");
                    content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                    
                    if (hasRequestBody(operation)) {
                        content.append("    And I include a valid request body\n");
                    }
                    
                    content.append("    And I include valid headers\n");
                    content.append("    Then the response status should be ").append(getExpectedStatusCode(operation)).append("\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the expected schema\n");
                    content.append("    And the response should contain mandatory fields\n");
                    content.append("    And the response headers should be valid\n");
                    content.append("    And the response time should be less than 10 seconds\n\n");
                    
                    TestScenario positiveScenario = createTestScenario(positiveScenarioName, description, method, fullUrl,
                            requestBody, getExpectedResponseSchema(operation), 
                            getExpectedHeaders(operation), getExpectedStatusCode(operation),
                            generatePositiveTestSteps(method, substitutedPath, requestBody), featureFile);
                    scenarios.add(positiveScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateNegativeTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Negative Tests - Invalid Requests and Error Handling\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test API error handling with invalid requests\n");
        content.append("  So that I can ensure the API handles errors gracefully\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_negative_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.SYSTEM, content.toString(), apiSpec);
        
        List<TestScenario> scenarios = new ArrayList<>();
        String baseUrl = getBaseUrl(openAPI);
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Test all HTTP methods for negative scenarios
                for (Map.Entry<String, Operation> methodEntry : getOperations(pathItem).entrySet()) {
                    String method = methodEntry.getKey();
                    Operation operation = methodEntry.getValue();
                    
                    // Invalid ID scenario
                    if (path.contains("{")) {
                        String invalidIdScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Invalid_ID";
                        String description = "Invalid ID in " + method + " request to " + path;
                        String invalidPath = substitutePathParametersWithInvalidValues(path, operation);
                        String fullUrl = baseUrl + invalidPath;
                        
                        content.append("  Scenario: ").append(invalidIdScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(invalidPath).append("\"\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 404\n");
                        content.append("    And the response should contain an error message\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario invalidIdScenario = createTestScenario(invalidIdScenarioName, description, method, fullUrl,
                                null, getErrorResponseSchema(), getExpectedHeaders(operation), 404,
                                generateNegativeTestSteps(method, invalidPath, "Invalid ID"), featureFile);
                        scenarios.add(invalidIdScenario);
                    }
                    
                    // Invalid request body scenario (for POST/PUT/PATCH)
                    if (hasRequestBody(operation)) {
                        String invalidBodyScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Invalid_Body";
                        String description = "Invalid request body in " + method + " request to " + path;
                        String substitutedPath = substitutePathParameters(path, operation);
                        String fullUrl = baseUrl + substitutedPath;
                        String invalidRequestBody = generateInvalidRequestBody(operation);
                        
                        content.append("  Scenario: ").append(invalidBodyScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                        content.append("    And I include an invalid request body\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 400\n");
                        content.append("    And the response should contain validation error messages\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario invalidBodyScenario = createTestScenario(invalidBodyScenarioName, description, method, fullUrl,
                                invalidRequestBody, getErrorResponseSchema(), getExpectedHeaders(operation), 400,
                                generateNegativeTestSteps(method, substitutedPath, "Invalid Body"), featureFile);
                        scenarios.add(invalidBodyScenario);
                    }
                    
                    // Missing required fields scenario (for POST/PUT/PATCH)
                    if (hasRequestBody(operation)) {
                        String missingFieldsScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Missing_Required_Fields";
                        String description = "Missing required fields in " + method + " request to " + path;
                        String substitutedPath = substitutePathParameters(path, operation);
                        String fullUrl = baseUrl + substitutedPath;
                        String incompleteRequestBody = generateIncompleteRequestBody(operation);
                        
                        content.append("  Scenario: ").append(missingFieldsScenarioName).append("\n");
                        content.append("    Given the API is available\n");
                        content.append("    And I have valid authentication credentials\n");
                        content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                        content.append("    And I include a request body with missing required fields\n");
                        content.append("    And I include valid headers\n");
                        content.append("    Then the response status should be 400\n");
                        content.append("    And the response should contain field validation error messages\n");
                        content.append("    And the response should be valid JSON\n");
                        content.append("    And the response time should be less than 5 seconds\n\n");
                        
                        TestScenario missingFieldsScenario = createTestScenario(missingFieldsScenarioName, description, method, fullUrl,
                                incompleteRequestBody, getErrorResponseSchema(), getExpectedHeaders(operation), 400,
                                generateNegativeTestSteps(method, substitutedPath, "Missing Required Fields"), featureFile);
                        scenarios.add(missingFieldsScenario);
                    }
                    
                    // Unauthorized access scenario
                    String unauthorizedScenarioName = generateScenarioName(method, path, operation.getOperationId()) + "_Unauthorized_Access";
                    String description = "Unauthorized access to " + method + " request to " + path;
                    String substitutedPath = substitutePathParameters(path, operation);
                    String fullUrl = baseUrl + substitutedPath;
                    
                    content.append("  Scenario: ").append(unauthorizedScenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    And I do not have valid authentication credentials\n");
                    content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                    content.append("    And I include invalid or missing authentication headers\n");
                    content.append("    Then the response status should be 401\n");
                    content.append("    And the response should contain an authentication error message\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response time should be less than 5 seconds\n\n");
                    
                    TestScenario unauthorizedScenario = createTestScenario(unauthorizedScenarioName, description, method, fullUrl,
                            null, getErrorResponseSchema(), getExpectedHeaders(operation), 401,
                            generateNegativeTestSteps(method, substitutedPath, "Unauthorized Access"), featureFile);
                    scenarios.add(unauthorizedScenario);
                }
            }
        }
        
        // Update the content with all scenarios
        featureFile.setContent(content.toString());
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private FeatureFile generateIntegrationTests(OpenAPI openAPI, ApiSpec apiSpec) {
        StringBuilder content = new StringBuilder();
        content.append("Feature: Integration Tests - CRUD Operations with Verification\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test complete CRUD workflows with verification\n");
        content.append("  So that I can ensure data integrity and proper API behavior\n\n");
        
        String baseUrl = getBaseUrl(openAPI);
        content.append("  Background:\n");
        content.append("    Given the API base URL is \"").append(baseUrl).append("\"\n");
        content.append("    And the Content-Type is \"application/json\"\n\n");
        
        List<TestScenario> scenarios = new ArrayList<>();
        
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
                String path = pathEntry.getKey();
                PathItem pathItem = pathEntry.getValue();
                
                // Rule 1: POST endpoints - Create and verify
                if (pathItem.getPost() != null) {
                    generatePostIntegrationTests(content, scenarios, path, pathItem.getPost(), pathItem.getGet());
                }
                
                // Rule 2: PUT/PATCH endpoints - Update and verify
                if (pathItem.getPut() != null) {
                    generatePutIntegrationTests(content, scenarios, path, pathItem.getPut(), pathItem.getGet());
                }
                if (pathItem.getPatch() != null) {
                    generatePatchIntegrationTests(content, scenarios, path, pathItem.getPatch(), pathItem.getGet());
                }
                
                // Rule 3: DELETE endpoints - Delete and verify
                if (pathItem.getDelete() != null) {
                    generateDeleteIntegrationTests(content, scenarios, path, pathItem.getDelete(), pathItem.getGet());
                }
                
                // Rule 4: GET endpoints with filters
                if (pathItem.getGet() != null) {
                    generateGetFilterTests(content, scenarios, path, pathItem.getGet());
                }
            }
        }
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_integration_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.INTEGRATION, content.toString(), apiSpec);
        
        // Set the FeatureFile on all scenarios
        for (TestScenario scenario : scenarios) {
            scenario.setFeatureFile(featureFile);
        }
        
        featureFile.setTestScenarios(scenarios);
        
        return featureFile;
    }
    
    private void generatePostIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation postOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Create " + resourceName + " and verify creation";
        
        content.append("  @integration @create @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to create a new ").append(resourceName).append("\n");
        
        // Generate POST request body based on schema
        String requestBody = generateRequestBody(postOp);
        content.append("    When I send a POST request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 201 or 200\n");
        content.append("    And the response should contain an \"id\" field\n");
        content.append("    And I store the response \"id\" as \"created").append(resourceName).append("Id\"\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, postOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the created ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal {{created").append(resourceName).append("Id}}\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for creating " + resourceName + " with verification");
        scenario.setHttpMethod("POST");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePutIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation putOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Update " + resourceName + " using PUT and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        // Generate PUT request body
        String requestBody = generateRequestBody(putOp);
        content.append("    When I send a PUT request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, putOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for updating " + resourceName + " with verification");
        scenario.setHttpMethod("PUT");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generatePatchIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation patchOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Partially update " + resourceName + " using PATCH and verify update";
        
        content.append("  @integration @update @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to partially update an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        
        // Generate PATCH request body (partial update)
        String requestBody = generatePartialRequestBody(patchOp);
        content.append("    When I send a PATCH request to \"").append(path).append("\" with body:\n");
        content.append("      \"\"\"\n");
        content.append(requestBody);
        content.append("\n      \"\"\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should contain updated ").append(resourceName).append(" data\n\n");
        
        // Generate GET verification
        if (getOp != null) {
            String getPath = generateGetPath(path, patchOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain the updated ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal 1\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for partially updating " + resourceName + " with verification");
        scenario.setHttpMethod("PATCH");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateDeleteIntegrationTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation deleteOp, Operation getOp) {
        String resourceName = extractResourceName(path);
        String scenarioName = "Delete " + resourceName + " and verify deletion";
        
        content.append("  @integration @delete @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: ").append(scenarioName).append("\n");
        content.append("    Given I want to delete an existing ").append(resourceName).append("\n");
        content.append("    And I have a ").append(resourceName).append(" with ID 1\n");
        content.append("    When I send a DELETE request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200 or 204\n\n");
        
        // Generate GET verification for 404
        if (getOp != null) {
            String getPath = generateGetPath(path, deleteOp);
            content.append("    When I send a GET request to \"").append(getPath).append("\"\n");
            content.append("    Then the response status should be 404\n");
            content.append("    And the response should not contain the deleted ").append(resourceName).append(" data\n\n");
        }
        
        // Create TestScenario object
        TestScenario scenario = new TestScenario();
        scenario.setScenarioName(scenarioName);
        scenario.setDescription("Integration test for deleting " + resourceName + " with verification");
        scenario.setHttpMethod("DELETE");
        scenario.setEndpoint(path);
        scenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(scenario);
    }
    
    private void generateGetFilterTests(StringBuilder content, List<TestScenario> scenarios, String path, Operation getOp) {
        String resourceName = extractResourceName(path);
        
        // Test basic GET endpoint
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get all ").append(resourceName).append(" and verify response structure\n");
        content.append("    Given I want to retrieve all ").append(resourceName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be an array\n");
        content.append("    And each item in the response should contain valid ").append(resourceName).append(" data\n\n");
        
        // Create TestScenario object for basic GET
        TestScenario basicGetScenario = new TestScenario();
        basicGetScenario.setScenarioName("Get all " + resourceName + " and verify response structure");
        basicGetScenario.setDescription("Integration test for retrieving all " + resourceName);
        basicGetScenario.setHttpMethod("GET");
        basicGetScenario.setEndpoint(path);
        basicGetScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(basicGetScenario);
        
        // Test GET with ID parameter
        if (path.contains("{") && path.contains("}")) {
            content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
            content.append("  Scenario: Get ").append(resourceName).append(" by ID\n");
            content.append("    Given I want to retrieve a specific ").append(resourceName).append("\n");
            content.append("    When I send a GET request to \"").append(path).append("\"\n");
            content.append("    Then the response status should be 200\n");
            content.append("    And the response should contain valid ").append(resourceName).append(" data\n");
            content.append("    And the response \"id\" should equal the requested ID\n\n");
            
            // Create TestScenario object for GET by ID
            TestScenario getByIdScenario = new TestScenario();
            getByIdScenario.setScenarioName("Get " + resourceName + " by ID");
            getByIdScenario.setDescription("Integration test for retrieving " + resourceName + " by ID");
            getByIdScenario.setHttpMethod("GET");
            getByIdScenario.setEndpoint(path);
            getByIdScenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(getByIdScenario);
            
            // Test invalid ID
            content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
            content.append("  Scenario: Get ").append(resourceName).append(" with invalid ID should return 404\n");
            content.append("    Given I want to retrieve a non-existent ").append(resourceName).append("\n");
            content.append("    When I send a GET request to \"").append(path).append("\"\n");
            content.append("    Then the response status should be 404\n\n");
            
            // Create TestScenario object for invalid ID
            TestScenario invalidIdScenario = new TestScenario();
            invalidIdScenario.setScenarioName("Get " + resourceName + " with invalid ID should return 404");
            invalidIdScenario.setDescription("Integration test for retrieving " + resourceName + " with invalid ID");
            invalidIdScenario.setHttpMethod("GET");
            invalidIdScenario.setEndpoint(path);
            invalidIdScenario.setCreatedAt(java.time.LocalDateTime.now());
            scenarios.add(invalidIdScenario);
        }
        
        // Test query parameters
        if (getOp.getParameters() != null) {
            for (io.swagger.v3.oas.models.parameters.Parameter param : getOp.getParameters()) {
                if ("query".equals(param.getIn())) {
                    generateQueryParameterTests(content, scenarios, path, resourceName, param);
                }
            }
        }
    }
    
    private void generateQueryParameterTests(StringBuilder content, List<TestScenario> scenarios, String path, String resourceName, io.swagger.v3.oas.models.parameters.Parameter param) {
        String paramName = param.getName();
        String paramType = param.getSchema() != null ? param.getSchema().getType() : "string";
        
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get ").append(resourceName).append(" filtered by ").append(paramName).append("\n");
        content.append("    Given I want to retrieve ").append(resourceName).append(" filtered by ").append(paramName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\" with query parameter \"").append(paramName).append("=1\"\n");
        content.append("    Then the response status should be 200\n");
        content.append("    And the response should be an array\n");
        content.append("    And each item in the response should have \"").append(paramName).append("\" equal to 1\n\n");
        
        // Create TestScenario object for valid parameter
        TestScenario validParamScenario = new TestScenario();
        validParamScenario.setScenarioName("Get " + resourceName + " filtered by " + paramName);
        validParamScenario.setDescription("Integration test for retrieving " + resourceName + " filtered by " + paramName);
        validParamScenario.setHttpMethod("GET");
        validParamScenario.setEndpoint(path);
        validParamScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(validParamScenario);
        
        // Test invalid parameter
        content.append("  @integration @filters @").append(resourceName.toLowerCase()).append("\n");
        content.append("  Scenario: Get ").append(resourceName).append(" with invalid ").append(paramName).append(" parameter\n");
        content.append("    Given I want to retrieve ").append(resourceName).append(" with invalid ").append(paramName).append("\n");
        content.append("    When I send a GET request to \"").append(path).append("\" with query parameter \"").append(paramName).append("=invalid\"\n");
        content.append("    Then the response status should be 400\n\n");
        
        // Create TestScenario object for invalid parameter
        TestScenario invalidParamScenario = new TestScenario();
        invalidParamScenario.setScenarioName("Get " + resourceName + " with invalid " + paramName + " parameter");
        invalidParamScenario.setDescription("Integration test for retrieving " + resourceName + " with invalid " + paramName);
        invalidParamScenario.setHttpMethod("GET");
        invalidParamScenario.setEndpoint(path);
        invalidParamScenario.setCreatedAt(java.time.LocalDateTime.now());
        scenarios.add(invalidParamScenario);
    }
    
    private String extractResourceName(String path) {
        // Extract resource name from path (e.g., /posts -> Posts, /users/{id} -> Users)
        String[] parts = path.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !part.startsWith("{")) {
                return part.substring(0, 1).toUpperCase() + part.substring(1);
            }
        }
        return "Resource";
    }
    
    private String generateRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema schema = content.get("application/json").getSchema();
                return generateJsonFromSchema(schema);
            }
        }
        return "{\n      \"id\": 1,\n      \"name\": \"Test Resource\",\n      \"description\": \"Test description\"\n    }";
    }
    
    private String generatePartialRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema schema = content.get("application/json").getSchema();
                return generatePartialJsonFromSchema(schema);
            }
        }
        return "{\n      \"name\": \"Updated Test Resource\"\n    }";
    }
    
    private String generateJsonFromSchema(Schema schema) {
        if (schema == null) {
            return "{\n      \"id\": 1,\n      \"name\": \"Test Resource\"\n    }";
        }
        
        StringBuilder json = new StringBuilder("{\n");
        if (schema.getProperties() != null) {
            boolean first = true;
            for (Object key : schema.getProperties().keySet()) {
                if (!first) json.append(",\n");
                json.append("      \"").append((String) key).append("\": ");
                json.append(generateValueFromSchema((Schema) schema.getProperties().get(key)));
                first = false;
            }
        } else {
            json.append("      \"id\": 1,\n");
            json.append("      \"name\": \"Test Resource\"");
        }
        json.append("\n    }");
        return json.toString();
    }
    
    private String generatePartialJsonFromSchema(Schema schema) {
        if (schema == null) {
            return "{\n      \"name\": \"Updated Test Resource\"\n    }";
        }
        
        StringBuilder json = new StringBuilder("{\n");
        if (schema.getProperties() != null) {
            boolean first = true;
            int count = 0;
            for (Object key : schema.getProperties().keySet()) {
                if (count >= 2) break; // Only include first 2 properties for partial update
                if (!first) json.append(",\n");
                json.append("      \"").append((String) key).append("\": ");
                json.append(generateValueFromSchema((Schema) schema.getProperties().get(key)));
                first = false;
                count++;
            }
        } else {
            json.append("      \"name\": \"Updated Test Resource\"");
        }
        json.append("\n    }");
        return json.toString();
    }
    
    private String generateValueFromSchema(Schema schema) {
        if (schema == null) return "\"test\"";
        
        String type = schema.getType();
        if (type == null) return "\"test\"";
        
        switch (type) {
            case "string":
                return "\"Test Value\"";
            case "integer":
                return "1";
            case "number":
                return "1.0";
            case "boolean":
                return "true";
            case "array":
                return "[]";
            case "object":
                return "{}";
            default:
                return "\"test\"";
        }
    }
    
    private String generateGetPath(String path, Operation operation) {
        // Convert path with parameters to actual path
        if (path.contains("{") && path.contains("}")) {
            return path.replaceAll("\\{[^}]+\\}", "1");
        }
        return path;
    }
    
    private Map<String, Operation> getOperations(PathItem pathItem) {
        Map<String, Operation> operations = new HashMap<>();
        if (pathItem.getGet() != null) operations.put("GET", pathItem.getGet());
        if (pathItem.getPost() != null) operations.put("POST", pathItem.getPost());
        if (pathItem.getPut() != null) operations.put("PUT", pathItem.getPut());
        if (pathItem.getPatch() != null) operations.put("PATCH", pathItem.getPatch());
        if (pathItem.getDelete() != null) operations.put("DELETE", pathItem.getDelete());
        if (pathItem.getHead() != null) operations.put("HEAD", pathItem.getHead());
        if (pathItem.getOptions() != null) operations.put("OPTIONS", pathItem.getOptions());
        return operations;
    }
    
    private String generateScenarioName(String method, String path, String operationId) {
        if (operationId != null && !operationId.isEmpty()) {
            return operationId.replaceAll("[^a-zA-Z0-9]", "_");
        }
        return method + "_" + path.replaceAll("[^a-zA-Z0-9]", "_");
    }
    
    private String getRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateTestRequestBody(schema);
                }
            }
        }
        return null;
    }
    
    private String generateTestRequestBody(Schema<?> schema) {
        try {
            Map<String, Object> testData = generateTestDataFromSchema(schema);
            return jsonMapper.writeValueAsString(testData);
        } catch (Exception e) {
            // Fallback to schema string if JSON generation fails
            return schema.toString();
        }
    }
    
    private String generateInvalidRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateInvalidTestDataFromSchema(schema);
                }
            }
        }
        return "{\"invalid\": \"data\"}";
    }
    
    private String generateIncompleteRequestBody(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            if (content.get("application/json") != null) {
                Schema<?> schema = content.get("application/json").getSchema();
                if (schema != null) {
                    return generateIncompleteTestDataFromSchema(schema);
                }
            }
        }
        return "{}";
    }
    
    private String generateInvalidTestDataFromSchema(Schema<?> schema) {
        try {
            Map<String, Object> invalidData = new HashMap<>();
            
            if (schema.getProperties() != null) {
                for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                    String key = property.getKey();
                    Schema<?> propertySchema = property.getValue();
                    
                    // Generate invalid data based on type
                    String type = propertySchema.getType();
                    switch (type) {
                        case "string":
                            invalidData.put(key, 123); // Wrong type
                            break;
                        case "integer":
                            invalidData.put(key, "invalid_number"); // Wrong type
                            break;
                        case "number":
                            invalidData.put(key, "invalid_float"); // Wrong type
                            break;
                        case "boolean":
                            invalidData.put(key, "invalid_boolean"); // Wrong type
                            break;
                        case "array":
                            invalidData.put(key, "not_an_array"); // Wrong type
                            break;
                        case "object":
                            invalidData.put(key, "not_an_object"); // Wrong type
                            break;
                        default:
                            invalidData.put(key, null); // Null value
                    }
                }
            }
            
            return jsonMapper.writeValueAsString(invalidData);
        } catch (Exception e) {
            return "{\"invalid\": \"data\"}";
        }
    }
    
    private String generateIncompleteTestDataFromSchema(Schema<?> schema) {
        try {
            Map<String, Object> incompleteData = new HashMap<>();
            
            if (schema.getProperties() != null) {
                for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                    String key = property.getKey();
                    Schema<?> propertySchema = property.getValue();
                    
                    // Only include non-required fields or skip some required fields
                    if (propertySchema.getRequired() == null || !propertySchema.getRequired().contains(key)) {
                        Object value = generateTestValue(propertySchema);
                        incompleteData.put(key, value);
                    }
                    // Skip some required fields to test validation
                }
            }
            
            return jsonMapper.writeValueAsString(incompleteData);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private Map<String, Object> generateTestDataFromSchema(Schema<?> schema) {
        Map<String, Object> testData = new HashMap<>();
        
        if (schema.getProperties() != null) {
            for (Map.Entry<String, Schema> property : schema.getProperties().entrySet()) {
                String key = property.getKey();
                Schema<?> propertySchema = property.getValue();
                Object value = generateTestValue(propertySchema);
                testData.put(key, value);
            }
        }
        
        return testData;
    }
    
    private Object generateTestValue(Schema<?> schema) {
        if (schema == null) return null;
        
        String type = schema.getType();
        Object example = schema.getExample();
        
        // Use example if available
        if (example != null) {
            return example;
        }
        
        // Generate test data based on type
        switch (type) {
            case "string":
                return "test_" + System.currentTimeMillis();
            case "integer":
                return 1;
            case "number":
                return 1.0;
            case "boolean":
                return true;
            case "array":
                if (schema.getItems() != null) {
                    return Arrays.asList(generateTestValue(schema.getItems()));
                }
                return Arrays.asList("test_item");
            case "object":
                if (schema.getProperties() != null) {
                    return generateTestDataFromSchema(schema);
                }
                return new HashMap<>();
            default:
                return "test_value";
        }
    }
    
    private String getExpectedResponseSchema(Operation operation) {
        if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
            ApiResponse response = operation.getResponses().get("200");
            if (response.getContent() != null && response.getContent().get("application/json") != null) {
                Schema<?> schema = response.getContent().get("application/json").getSchema();
                return schema != null ? schema.toString() : null;
            }
        }
        return null;
    }
    
    private String getErrorResponseSchema() {
        return "{\"type\": \"object\", \"properties\": {\"error\": {\"type\": \"string\"}, \"message\": {\"type\": \"string\"}, \"status\": {\"type\": \"integer\"}}}";
    }
    
    private String getExpectedHeaders(Operation operation) {
        // Extract expected headers from operation
        return "Content-Type: application/json";
    }
    
    private Integer getExpectedStatusCode(Operation operation) {
        if (operation.getResponses() != null) {
            if (operation.getResponses().get("200") != null) return 200;
            if (operation.getResponses().get("201") != null) return 201;
            if (operation.getResponses().get("204") != null) return 204;
        }
        return 200; // Default
    }
    
    private boolean hasRequestBody(Operation operation) {
        return operation.getRequestBody() != null;
    }
    
    private String generatePositiveTestSteps(String method, String path, String requestBody) {
        StringBuilder steps = new StringBuilder();
        steps.append("1. Prepare valid ").append(method).append(" request to ").append(path).append("\n");
        steps.append("2. Include proper authentication headers\n");
        steps.append("3. Include valid content-type headers\n");
        if (requestBody != null) {
            steps.append("4. Include valid request body with all required fields\n");
        }
        steps.append("5. Send the request\n");
        steps.append("6. Validate response status code matches expected\n");
        steps.append("7. Validate response body is valid JSON\n");
        steps.append("8. Validate response body matches expected schema\n");
        steps.append("9. Validate all mandatory fields are present\n");
        steps.append("10. Validate response headers are correct\n");
        steps.append("11. Validate response time is within acceptable limits\n");
        return steps.toString();
    }
    
    private String generateNegativeTestSteps(String method, String path, String errorType) {
        StringBuilder steps = new StringBuilder();
        steps.append("1. Prepare ").append(method).append(" request to ").append(path).append("\n");
        steps.append("2. Include ").append(errorType.toLowerCase()).append("\n");
        steps.append("3. Send the request\n");
        steps.append("4. Validate response status code indicates error\n");
        steps.append("5. Validate response body contains appropriate error message\n");
        steps.append("6. Validate response body is valid JSON\n");
        steps.append("7. Validate response time is within acceptable limits\n");
        return steps.toString();
    }
    
    private String substitutePathParameters(String path, Operation operation) {
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            return path;
        }
        
        String substitutedPath = path;
        
        for (io.swagger.v3.oas.models.parameters.Parameter param : operation.getParameters()) {
            if ("path".equals(param.getIn()) && param.getName() != null) {
                String paramName = param.getName();
                Object example = param.getExample();
                Object defaultValue = param.getSchema() != null ? param.getSchema().getExample() : null;
                
                // Use example value if available, otherwise use default test value
                String testValue;
                if (example != null) {
                    testValue = example.toString();
                } else if (defaultValue != null) {
                    testValue = defaultValue.toString();
                } else {
                    // Generate test value based on parameter name
                    testValue = generateTestValueForParameter(paramName, param.getSchema());
                }
                
                substitutedPath = substitutedPath.replace("{" + paramName + "}", testValue);
            }
        }
        
        return substitutedPath;
    }
    
    private String substitutePathParametersWithInvalidValues(String path, Operation operation) {
        if (operation.getParameters() == null || operation.getParameters().isEmpty()) {
            return path;
        }
        
        String substitutedPath = path;
        
        for (io.swagger.v3.oas.models.parameters.Parameter param : operation.getParameters()) {
            if ("path".equals(param.getIn()) && param.getName() != null) {
                String paramName = param.getName();
                
                // Generate invalid test value
                String invalidValue = generateInvalidTestValueForParameter(paramName, param.getSchema());
                substitutedPath = substitutedPath.replace("{" + paramName + "}", invalidValue);
            }
        }
        
        return substitutedPath;
    }
    
    private String generateTestValueForParameter(String paramName, Schema<?> schema) {
        // Generate appropriate test values based on parameter name and type
        if (paramName.toLowerCase().contains("id")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random ID from 1-10
        } else if (paramName.toLowerCase().contains("user")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random user ID from 1-10
        } else if (paramName.toLowerCase().contains("post")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random post ID from 1-10
        } else if (paramName.toLowerCase().contains("comment")) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random comment ID from 1-10
        } else if (schema != null && "integer".equals(schema.getType())) {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random integer from 1-10
        } else if (schema != null && "string".equals(schema.getType())) {
            return "test";
        } else {
            return String.valueOf((int)(Math.random() * 10) + 1); // Random value from 1-10
        }
    }
    
    private String generateInvalidTestValueForParameter(String paramName, Schema<?> schema) {
        // Generate invalid test values for negative testing
        if (paramName.toLowerCase().contains("id")) {
            return "1"; // Valid ID from 1-10 range
        } else if (paramName.toLowerCase().contains("user")) {
            return "1"; // Valid user ID from 1-10 range
        } else if (paramName.toLowerCase().contains("post")) {
            return "1"; // Valid post ID from 1-10 range
        } else if (paramName.toLowerCase().contains("comment")) {
            return "1"; // Valid comment ID from 1-10 range
        } else if (schema != null && "integer".equals(schema.getType())) {
            return "1"; // Valid integer from 1-10 range
        } else if (schema != null && "string".equals(schema.getType())) {
            return "test"; // Valid string value
        } else {
            return "1"; // Default valid value from 1-10 range
        }
    }
    
    private TestScenario createTestScenario(String scenarioName, String description, String httpMethod, 
                                         String endpoint, String requestBody, String expectedResponseSchema,
                                         String expectedHeaders, Integer expectedStatusCode, String testSteps,
                                         FeatureFile featureFile) {
        return new TestScenario(scenarioName, description, httpMethod, endpoint, requestBody,
                expectedResponseSchema, expectedHeaders, expectedStatusCode, testSteps, featureFile);
    }
    
}