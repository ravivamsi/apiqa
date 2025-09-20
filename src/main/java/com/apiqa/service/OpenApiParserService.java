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
            
            // Generate Smoke Tests (GET operations only)
            featureFiles.add(generateSmokeTests(openAPI, apiSpec));
            
            // Generate System Tests (All CRUD operations)
            featureFiles.add(generateSystemTests(openAPI, apiSpec));
            
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
        content.append("Feature: Smoke Tests - Read-only Operations\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to verify basic API functionality\n");
        content.append("  So that I can ensure the API is accessible and responding\n\n");
        
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
                    String scenarioName = generateScenarioName("GET", path, getOp.getOperationId());
                    String description = getOp.getSummary() != null ? getOp.getSummary() : "GET " + path;
                    String substitutedPath = substitutePathParameters(path, getOp);
                    String fullUrl = baseUrl + substitutedPath;
                    
                    content.append("  Scenario: ").append(scenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    When I send a GET request to \"").append(substitutedPath).append("\"\n");
                    content.append("    Then the response status should be 200\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the schema\n\n");
                    
                    TestScenario scenario = createTestScenario(scenarioName, description, "GET", fullUrl, 
                            null, getExpectedResponseSchema(getOp), null, 200, 
                            generateTestSteps("GET", substitutedPath, null), featureFile);
                    scenarios.add(scenario);
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
        content.append("Feature: System Tests - All CRUD Operations\n");
        content.append("  As a QA Engineer\n");
        content.append("  I want to test all API operations thoroughly\n");
        content.append("  So that I can ensure complete API functionality\n\n");
        
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
                    
                    String scenarioName = generateScenarioName(method, path, operation.getOperationId());
                    String description = operation.getSummary() != null ? operation.getSummary() : method + " " + path;
                    String substitutedPath = substitutePathParameters(path, operation);
                    String fullUrl = baseUrl + substitutedPath;
                    String requestBody = getRequestBody(operation);
                    
                    content.append("  Scenario: ").append(scenarioName).append("\n");
                    content.append("    Given the API is available\n");
                    content.append("    When I send a ").append(method).append(" request to \"").append(substitutedPath).append("\"\n");
                    
                    if (hasRequestBody(operation)) {
                        content.append("    And I include the request body\n");
                    }
                    
                    content.append("    Then the response status should be valid\n");
                    content.append("    And the response should be valid JSON\n");
                    content.append("    And the response should match the schema\n");
                    content.append("    And the response headers should be valid\n\n");
                    
                    TestScenario scenario = createTestScenario(scenarioName, description, method, fullUrl,
                            requestBody, getExpectedResponseSchema(operation), 
                            getExpectedHeaders(operation), getExpectedStatusCode(operation),
                            generateTestSteps(method, substitutedPath, requestBody), featureFile);
                    scenarios.add(scenario);
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
        content.append("    When I create a new resource\n");
        content.append("    Then the resource should be created successfully\n");
        content.append("    When I retrieve the created resource\n");
        content.append("    Then the resource should be returned correctly\n");
        content.append("    When I update the resource\n");
        content.append("    Then the resource should be updated successfully\n");
        content.append("    When I delete the resource\n");
        content.append("    Then the resource should be deleted successfully\n\n");
        
        content.append("  Scenario: Authentication and Authorization Flow\n");
        content.append("    Given the API requires authentication\n");
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
    
    private String generateTestSteps(String method, String path, String requestBody) {
        StringBuilder steps = new StringBuilder();
        steps.append("1. Send ").append(method).append(" request to ").append(path).append("\n");
        steps.append("2. Validate response status code\n");
        steps.append("3. Validate response body schema\n");
        steps.append("4. Validate response headers\n");
        if (requestBody != null) {
            steps.append("5. Validate request body format\n");
        }
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
    
    private TestScenario createTestScenario(String scenarioName, String description, String httpMethod, 
                                         String endpoint, String requestBody, String expectedResponseSchema,
                                         String expectedHeaders, Integer expectedStatusCode, String testSteps,
                                         FeatureFile featureFile) {
        return new TestScenario(scenarioName, description, httpMethod, endpoint, requestBody,
                expectedResponseSchema, expectedHeaders, expectedStatusCode, testSteps, featureFile);
    }
}
