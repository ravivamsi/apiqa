package com.apiqa.controller;

import com.apiqa.model.ApiSpec;
import com.apiqa.model.FeatureFile;
import com.apiqa.service.ApiQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private ApiQaService apiQaService;
    
    @GetMapping("/spec/{id}/content")
    public ResponseEntity<String> getSpecContent(@PathVariable Long id) {
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
            result.append("Content:\n").append(featureFile.getContent()).append("\n\n");
        }
        
        return ResponseEntity.ok(result.toString());
    }
}
