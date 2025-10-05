package com.apiqa.controller;

import com.apiqa.model.*;
import com.apiqa.service.AdminService;
import com.apiqa.service.ApiQaService;
import com.apiqa.service.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private ApiQaService apiQaService;
    
    @Autowired
    private EnvironmentService environmentService;
    
    @GetMapping
    public String adminDashboard(Model model) {
        List<TestSuite> testSuites = adminService.getAllTestSuites();
        List<CustomEndpoint> customEndpoints = adminService.getAllCustomEndpoints();
        List<TestCase> testCases = adminService.getAllTestCases();
        List<ApiSpec> apiSpecs = apiQaService.getAllApiSpecs();
        List<Environment> environments = environmentService.getAllEnvironments();
        
        model.addAttribute("testSuites", testSuites);
        model.addAttribute("customEndpoints", customEndpoints);
        model.addAttribute("testCases", testCases);
        model.addAttribute("apiSpecs", apiSpecs);
        model.addAttribute("environments", environments);
        
        return "admin";
    }
    
    // Test Suite Management
    @PostMapping("/test-suites")
    public String createTestSuite(@RequestParam String name, 
                                 @RequestParam String description,
                                 @RequestParam String testType,
                                 @RequestParam String createdBy) {
        try {
            TestType type = TestType.valueOf(testType);
            adminService.createTestSuite(name, description, type, createdBy);
            return "redirect:/admin?success=Test Suite created successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to create Test Suite: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-suites/{id}/update")
    public String updateTestSuite(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String description,
                                 @RequestParam String testType) {
        try {
            TestType type = TestType.valueOf(testType);
            adminService.updateTestSuite(id, name, description, type);
            return "redirect:/admin?success=Test Suite updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update Test Suite: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-suites/{id}/delete")
    public String deleteTestSuite(@PathVariable Long id) {
        try {
            adminService.deleteTestSuite(id);
            return "redirect:/admin?success=Test Suite deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete Test Suite: " + e.getMessage();
        }
    }
    
    // Test Case Management
    @PostMapping("/test-cases")
    public String createTestCase(@RequestParam String name,
                               @RequestParam String description,
                               @RequestParam String httpMethod,
                               @RequestParam String endpoint,
                               @RequestParam(required = false) String requestBody,
                               @RequestParam(required = false) String expectedResponseSchema,
                               @RequestParam(required = false) String expectedHeaders,
                               @RequestParam(required = false) Integer expectedStatusCode,
                               @RequestParam String createdBy,
                               @RequestParam(required = false) Long apiSpecId,
                               @RequestParam(required = false) Long testSuiteId) {
        try {
            adminService.createTestCase(name, description, httpMethod, endpoint,
                    requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode,
                    createdBy, apiSpecId, testSuiteId);
            return "redirect:/admin?success=Test Case created successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to create Test Case: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-cases/{id}/update")
    public String updateTestCase(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam String httpMethod,
                               @RequestParam String endpoint,
                               @RequestParam(required = false) String requestBody,
                               @RequestParam(required = false) String expectedResponseSchema,
                               @RequestParam(required = false) String expectedHeaders,
                               @RequestParam(required = false) Integer expectedStatusCode,
                               @RequestParam(required = false) Long apiSpecId,
                               @RequestParam(required = false) Long testSuiteId) {
        try {
            adminService.updateTestCase(id, name, description, httpMethod, endpoint,
                    requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode,
                    apiSpecId, testSuiteId);
            return "redirect:/admin?success=Test Case updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update Test Case: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-cases/{id}/delete")
    public String deleteTestCase(@PathVariable Long id) {
        try {
            adminService.deleteTestCase(id);
            return "redirect:/admin?success=Test Case deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete Test Case: " + e.getMessage();
        }
    }
    
    // Custom Endpoint Management
    @PostMapping("/custom-endpoints")
    public String createCustomEndpoint(@RequestParam String name,
                                     @RequestParam String description,
                                     @RequestParam String httpMethod,
                                     @RequestParam String endpoint,
                                     @RequestParam(required = false) String requestBody,
                                     @RequestParam(required = false) String expectedResponseSchema,
                                     @RequestParam(required = false) String expectedHeaders,
                                     @RequestParam(required = false) Integer expectedStatusCode,
                                     @RequestParam String createdBy,
                                     @RequestParam(required = false) Long apiSpecId,
                                     @RequestParam(required = false) Long testSuiteId) {
        try {
            adminService.createCustomEndpoint(name, description, httpMethod, endpoint,
                    requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode,
                    createdBy, apiSpecId, testSuiteId);
            return "redirect:/admin?success=Custom Endpoint created successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to create Custom Endpoint: " + e.getMessage();
        }
    }
    
    @PostMapping("/custom-endpoints/{id}/update")
    public String updateCustomEndpoint(@PathVariable Long id,
                                     @RequestParam String name,
                                     @RequestParam String description,
                                     @RequestParam String httpMethod,
                                     @RequestParam String endpoint,
                                     @RequestParam(required = false) String requestBody,
                                     @RequestParam(required = false) String expectedResponseSchema,
                                     @RequestParam(required = false) String expectedHeaders,
                                     @RequestParam(required = false) Integer expectedStatusCode,
                                     @RequestParam(required = false) Long apiSpecId,
                                     @RequestParam(required = false) Long testSuiteId) {
        try {
            adminService.updateCustomEndpoint(id, name, description, httpMethod, endpoint,
                    requestBody, expectedResponseSchema, expectedHeaders, expectedStatusCode,
                    apiSpecId, testSuiteId);
            return "redirect:/admin?success=Custom Endpoint updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update Custom Endpoint: " + e.getMessage();
        }
    }
    
    @PostMapping("/custom-endpoints/{id}/delete")
    public String deleteCustomEndpoint(@PathVariable Long id) {
        try {
            adminService.deleteCustomEndpoint(id);
            return "redirect:/admin?success=Custom Endpoint deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete Custom Endpoint: " + e.getMessage();
        }
    }
    
    // Test Case Step Management
    @PostMapping("/test-case-steps")
    public String createTestCaseStep(@RequestParam String stepName,
                                   @RequestParam String description,
                                   @RequestParam String stepType,
                                   @RequestParam(required = false) String expectedValue,
                                   @RequestParam(required = false) String jsonPath,
                                   @RequestParam(required = false) String assertionType,
                                   @RequestParam(required = false) String assertionValue,
                                   @RequestParam Integer stepOrder,
                                   @RequestParam String createdBy,
                                   @RequestParam(required = false) Long testCaseId) {
        try {
            adminService.createTestCaseStep(stepName, description, stepType, expectedValue,
                    jsonPath, assertionType, assertionValue, stepOrder, createdBy, testCaseId);
            return "redirect:/admin?success=Test Case Step created successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to create Test Case Step: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-case-steps/{id}/update")
    public String updateTestCaseStep(@PathVariable Long id,
                                   @RequestParam String stepName,
                                   @RequestParam String description,
                                   @RequestParam String stepType,
                                   @RequestParam(required = false) String expectedValue,
                                   @RequestParam(required = false) String jsonPath,
                                   @RequestParam(required = false) String assertionType,
                                   @RequestParam(required = false) String assertionValue,
                                   @RequestParam Integer stepOrder,
                                   @RequestParam(required = false) Long testCaseId) {
        try {
            adminService.updateTestCaseStep(id, stepName, description, stepType, expectedValue,
                    jsonPath, assertionType, assertionValue, stepOrder, testCaseId);
            return "redirect:/admin?success=Test Case Step updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update Test Case Step: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-case-steps/{id}/delete")
    public String deleteTestCaseStep(@PathVariable Long id) {
        try {
            adminService.deleteTestCaseStep(id);
            return "redirect:/admin?success=Test Case Step deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete Test Case Step: " + e.getMessage();
        }
    }
    
    // Execution Endpoints
    @PostMapping("/test-suites/{id}/execute")
    public String executeTestSuite(@PathVariable Long id, @RequestParam String runName) {
        try {
            adminService.executeTestSuite(id, runName);
            return "redirect:/admin?success=Test Suite execution started successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to execute Test Suite: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-cases/{id}/execute")
    public String executeTestCase(@PathVariable Long id) {
        try {
            adminService.executeTestCase(id);
            return "redirect:/admin?success=Test Case executed successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to execute Test Case: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-case-steps/{id}/execute")
    public String executeTestCaseStep(@PathVariable Long id) {
        try {
            boolean result = adminService.executeTestCaseStep(id);
            return "redirect:/admin?success=Test Case Step executed successfully. Result: " + (result ? "PASSED" : "FAILED");
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to execute Test Case Step: " + e.getMessage();
        }
    }
    
    // API Endpoints for AJAX calls
    @GetMapping("/api/test-suites")
    @ResponseBody
    public ResponseEntity<List<TestSuite>> getAllTestSuites() {
        return ResponseEntity.ok(adminService.getAllTestSuites());
    }
    
    @GetMapping("/api/test-suites/{id}")
    @ResponseBody
    public ResponseEntity<TestSuite> getTestSuiteById(@PathVariable Long id) {
        Optional<TestSuite> testSuite = adminService.getTestSuiteById(id);
        return testSuite.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/custom-endpoints")
    @ResponseBody
    public ResponseEntity<List<CustomEndpoint>> getAllCustomEndpoints() {
        return ResponseEntity.ok(adminService.getAllCustomEndpoints());
    }
    
    @GetMapping("/api/custom-endpoints/{id}")
    @ResponseBody
    public ResponseEntity<CustomEndpoint> getCustomEndpointById(@PathVariable Long id) {
        Optional<CustomEndpoint> customEndpoint = adminService.getCustomEndpointById(id);
        return customEndpoint.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/test-cases")
    @ResponseBody
    public ResponseEntity<List<TestCase>> getAllTestCases() {
        return ResponseEntity.ok(adminService.getAllTestCases());
    }
    
    @GetMapping("/api/test-cases/{id}")
    @ResponseBody
    public ResponseEntity<TestCase> getTestCaseById(@PathVariable Long id) {
        Optional<TestCase> testCase = adminService.getTestCaseById(id);
        return testCase.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/test-cases/test-suite/{testSuiteId}")
    @ResponseBody
    public ResponseEntity<List<TestCase>> getTestCasesByTestSuite(@PathVariable Long testSuiteId) {
        return ResponseEntity.ok(adminService.getTestCasesByTestSuite(testSuiteId));
    }
    
    @GetMapping("/api/test-case-steps/test-case/{testCaseId}")
    @ResponseBody
    public ResponseEntity<List<TestCaseStep>> getTestCaseStepsByTestCase(@PathVariable Long testCaseId) {
        return ResponseEntity.ok(adminService.getTestCaseStepsByTestCase(testCaseId));
    }
    
    @PostMapping("/api/test-case-steps/reorder")
    @ResponseBody
    public ResponseEntity<String> reorderTestCaseSteps(@RequestParam Long testCaseId,
                                                     @RequestBody List<Long> stepIds) {
        try {
            adminService.reorderTestCaseSteps(testCaseId, stepIds);
            return ResponseEntity.ok("Test case steps reordered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reorder test case steps: " + e.getMessage());
        }
    }
    
    // Environment Management
    @PostMapping("/environments")
    public String createEnvironment(@RequestParam String name, 
                                   @RequestParam(required = false) String description,
                                   @RequestParam(required = false) String createdByName) {
        try {
            String creatorName = createdByName != null && !createdByName.trim().isEmpty() ? createdByName : "Admin";
            environmentService.createEnvironment(name, description, creatorName);
            return "redirect:/admin?success=Environment created successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to create environment: " + e.getMessage();
        }
    }
    
    @PostMapping("/environments/{id}")
    public String updateEnvironment(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam(required = false) String description) {
        try {
            environmentService.updateEnvironment(id, name, description);
            return "redirect:/admin?success=Environment updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update environment: " + e.getMessage();
        }
    }
    
    @PostMapping("/environments/{id}/delete")
    public String deleteEnvironment(@PathVariable Long id) {
        try {
            environmentService.deleteEnvironment(id);
            return "redirect:/admin?success=Environment deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete environment: " + e.getMessage();
        }
    }
    
    // Environment Variable Management
    @PostMapping("/environments/{id}/variables")
    public String addEnvironmentVariable(@PathVariable("id") Long environmentId,
                                        @RequestParam String key,
                                        @RequestParam String value,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false) String variableType,
                                        @RequestParam(required = false) String isSensitiveParam) {
        try {
            // Handle Boolean parsing manually
            Boolean isSensitive = false;
            if (isSensitiveParam != null && !isSensitiveParam.isEmpty()) {
                isSensitive = Boolean.parseBoolean(isSensitiveParam) || "true".equalsIgnoreCase(isSensitiveParam);
            }
            
            System.out.println("Adding variable - environmentId: " + environmentId + ", key: " + key + ", value: " + value);
            environmentService.addVariable(environmentId, key, value, description, variableType, isSensitive);
            return "redirect:/admin?success=Environment variable added successfully";
        } catch (Exception e) {
            System.err.println("Error adding environment variable: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin?error=Failed to add environment variable: " + e.getMessage();
        }
    }
    
    @PostMapping("/environments/variables/{id}")
    public String updateEnvironmentVariable(@PathVariable Long id,
                                            @RequestParam String value,
                                            @RequestParam(required = false) String description,
                                            @RequestParam(required = false) String variableType,
                                            @RequestParam(required = false, defaultValue = "false") Boolean isSensitive) {
        try {
            environmentService.updateVariable(id, value, description, variableType, isSensitive);
            return "redirect:/admin?success=Environment variable updated successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to update environment variable: " + e.getMessage();
        }
    }
    
    @PostMapping("/environments/variables/{id}/delete")
    public String deleteEnvironmentVariable(@PathVariable Long id) {
        try {
            environmentService.deleteVariable(id);
            return "redirect:/admin?success=Environment variable deleted successfully";
        } catch (Exception e) {
            return "redirect:/admin?error=Failed to delete environment variable: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/environments/{id}/variables")
    @ResponseBody
    public ResponseEntity<List<EnvironmentVariable>> getEnvironmentVariables(@PathVariable Long id) {
        try {
            List<EnvironmentVariable> variables = environmentService.getVariablesByEnvironmentId(id);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
