package com.apiqa.controller;

import com.apiqa.model.ApiSpec;
import com.apiqa.model.FeatureFile;
import com.apiqa.service.ApiQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/specs")
public class ApiSpecController {
    
    @Autowired
    private ApiQaService apiQaService;
    
    @GetMapping
    public ResponseEntity<List<ApiSpec>> getAllSpecs() {
        List<ApiSpec> specs = apiQaService.getAllApiSpecs();
        return ResponseEntity.ok(specs);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiSpec> getSpecById(@PathVariable Long id) {
        Optional<ApiSpec> spec = apiQaService.getApiSpecById(id);
        return spec.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ApiSpec> uploadSpec(@RequestBody ApiSpecRequest request) {
        try {
            ApiSpec spec = apiQaService.uploadApiSpec(
                    request.getName(),
                    request.getOpenApiYaml()
            );
            return ResponseEntity.ok(spec);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping(value = "/form", consumes = {"application/x-www-form-urlencoded", "multipart/form-data"})
    public ResponseEntity<String> uploadSpecForm(@RequestParam String name,
                                               @RequestParam String openApiYaml) {
        try {
            System.out.println("Received upload request:");
            System.out.println("Name: " + name);
            System.out.println("OpenAPI YAML length: " + (openApiYaml != null ? openApiYaml.length() : "null"));
            System.out.println("OpenAPI YAML preview: " + (openApiYaml != null ? openApiYaml.substring(0, Math.min(200, openApiYaml.length())) : "null"));
            
            ApiSpec spec = apiQaService.uploadApiSpec(name, openApiYaml);
            
            // Automatically generate tests after successful upload
            try {
                System.out.println("Auto-generating tests for spec ID: " + spec.getId());
                apiQaService.generateFeatureFiles(spec.getId());
                System.out.println("Tests generated successfully for spec ID: " + spec.getId());
            } catch (Exception e) {
                System.err.println("Warning: Failed to auto-generate tests: " + e.getMessage());
                e.printStackTrace();
                // Don't fail the upload if test generation fails
            }
            
            return ResponseEntity.ok("API Specification uploaded and tests generated successfully with ID: " + spec.getId());
        } catch (Exception e) {
            System.err.println("Error uploading API spec: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload API Specification: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/generate-tests")
    public ResponseEntity<String> generateTests(@PathVariable Long id) {
        try {
            apiQaService.generateFeatureFiles(id);
            return ResponseEntity.ok("Tests generated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to generate tests: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/debug-content")
    public ResponseEntity<String> debugContent(@PathVariable Long id) {
        try {
            Optional<ApiSpec> apiSpecOpt = apiQaService.getApiSpecById(id);
            if (apiSpecOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ApiSpec apiSpec = apiSpecOpt.get();
            StringBuilder result = new StringBuilder();
            result.append("API Spec: ").append(apiSpec.getName()).append("\n");
            result.append("Feature Files: ").append(apiSpec.getFeatureFiles().size()).append("\n\n");
            
            for (FeatureFile featureFile : apiSpec.getFeatureFiles()) {
                result.append("=== ").append(featureFile.getFileName()).append(" ===\n");
                result.append("Suite Type: ").append(featureFile.getSuiteType()).append("\n");
                result.append("Test Scenarios: ").append(featureFile.getTestScenarios().size()).append("\n");
                result.append("Content Length: ").append(featureFile.getContent().length()).append("\n");
                result.append("Content Preview (first 500 chars):\n").append(featureFile.getContent().substring(0, Math.min(500, featureFile.getContent().length()))).append("\n\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSpec(@PathVariable Long id) {
        try {
            apiQaService.deleteApiSpec(id);
            return ResponseEntity.ok("API Spec deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete API Spec: " + e.getMessage());
        }
    }
    
    public static class ApiSpecRequest {
        private String name;
        private String openApiYaml;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getOpenApiYaml() { return openApiYaml; }
        public void setOpenApiYaml(String openApiYaml) { this.openApiYaml = openApiYaml; }
    }
}
