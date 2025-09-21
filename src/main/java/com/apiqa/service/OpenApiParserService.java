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
        content.append("Feature: Integration Tests - End-to-End Workflows\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test complete user workflows\n");
        content.append("  So that I can ensure the API works end-to-end\n\n");
        
        List<TestScenario> scenarios = new ArrayList<>();
        
        // Generate workflow scenarios based on common patterns
        content.append("  Scenario: Complete CRUD Workflow\n");
        content.append("    Given the API is available\n");
        content.append("    And I have valid authentication credentials\n");
        content.append("    When I create a new resource\n");
        content.append("    Then the resource should be created successfully\n");
        content.append("    When I retrieve the created resource\n");
        content.append("    Then the resource should be returned correctly\n");
        content.append("    When I update the resource\n");
        content.append("    Then the resource should be updated successfully\n");
        content.append("    When I delete the resource\n");
        content.append("    Then the resource should be deleted successfully\n\n");
        
        content.append("  Scenario: Authentication and Authorization Flow\n");
        content.append("    Given the API is available\n");
        content.append("    When I authenticate with valid credentials\n");
        content.append("    Then I should receive a valid token\n");
        content.append("    When I make an authenticated request\n");
        content.append("    Then the request should be authorized\n\n");
        
        String fileName = apiSpec.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_integration_tests.feature";
        FeatureFile featureFile = new FeatureFile(fileName, TestSuiteType.INTEGRATION, content.toString(), apiSpec);
        
        return featureFile;
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
            return "1";
        } else if (paramName.toLowerCase().contains("user")) {
            return "1";
        } else if (paramName.toLowerCase().contains("post")) {
            return "1";
        } else if (paramName.toLowerCase().contains("comment")) {
            return "1";
        } else if (schema != null && "integer".equals(schema.getType())) {
            return "1";
        } else if (schema != null && "string".equals(schema.getType())) {
            return "test";
        } else {
            return "1"; // Default fallback
        }
    }
    
    private String generateInvalidTestValueForParameter(String paramName, Schema<?> schema) {
        // Generate invalid test values for negative testing
        if (paramName.toLowerCase().contains("id")) {
            return "999999"; // Non-existent ID
        } else if (paramName.toLowerCase().contains("user")) {
            return "999999"; // Non-existent user ID
        } else if (paramName.toLowerCase().contains("post")) {
            return "999999"; // Non-existent post ID
        } else if (paramName.toLowerCase().contains("comment")) {
            return "999999"; // Non-existent comment ID
        } else if (schema != null && "integer".equals(schema.getType())) {
            return "invalid_number"; // Invalid integer
        } else if (schema != null && "string".equals(schema.getType())) {
            return ""; // Empty string
        } else {
            return "invalid_value"; // Default invalid value
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