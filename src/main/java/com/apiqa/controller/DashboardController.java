package com.apiqa.controller;

import com.apiqa.model.ApiSpec;
import com.apiqa.model.TestRun;
import com.apiqa.model.TestExecution;
import com.apiqa.model.FeatureFile;
import com.apiqa.dto.TestExecutionDetailsDto;
import com.apiqa.service.ApiQaService;
import com.apiqa.service.TestExecutionService;
import com.apiqa.service.ScheduledTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class DashboardController {
    
    @Autowired
    private ApiQaService apiQaService;
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    @Autowired
    private ScheduledTestService scheduledTestService;
    
    @Autowired
    private com.apiqa.service.EmailService emailService;
    
    @GetMapping
    public String dashboard(Model model) {
        List<ApiSpec> apiSpecs = apiQaService.getAllApiSpecs();
        List<TestRun> recentTestRuns = apiQaService.getAllTestRuns();
        
        // Calculate pass rate
        long totalTestRuns = recentTestRuns.size();
        long successfulTestRuns = recentTestRuns.stream()
                .filter(run -> run.getStatus() == com.apiqa.model.TestRunStatus.COMPLETED)
                .count();
        
        double passRate = totalTestRuns > 0 ? (double) successfulTestRuns / totalTestRuns * 100 : 0.0;
        
        model.addAttribute("apiSpecs", apiSpecs);
        model.addAttribute("recentTestRuns", recentTestRuns);
        model.addAttribute("totalSpecs", apiSpecs.size());
        model.addAttribute("totalTestRuns", totalTestRuns);
        model.addAttribute("successfulTestRuns", successfulTestRuns);
        model.addAttribute("passRate", Math.round(passRate * 100.0) / 100.0);
        
        return "dashboard";
    }
    
    @GetMapping("/specs")
    public String specs(Model model) {
        List<ApiSpec> apiSpecs = apiQaService.getAllApiSpecs();
        model.addAttribute("apiSpecs", apiSpecs);
        return "specs";
    }
    
    @GetMapping("/specs/{id}")
    public String specDetails(@PathVariable Long id, Model model) {
        Optional<ApiSpec> apiSpec = apiQaService.getApiSpecById(id);
        if (apiSpec.isEmpty()) {
            return "redirect:/specs?error=Spec not found";
        }
        
        List<TestRun> testRuns = apiQaService.getTestRunsByApiSpecId(id);
        model.addAttribute("apiSpec", apiSpec.get());
        model.addAttribute("testRuns", testRuns);
        
        return "spec-details";
    }
    
    @GetMapping("/test-runs")
    public String testRuns(Model model) {
        List<TestRun> testRuns = apiQaService.getAllTestRuns();
        model.addAttribute("testRuns", testRuns);
        return "test-runs";
    }
    
    @GetMapping("/test-runs/{id}")
    public String testRunDetails(@PathVariable Long id, Model model) {
        Optional<TestRun> testRun = apiQaService.getTestRunById(id);
        if (testRun.isEmpty()) {
            return "redirect:/test-runs?error=Test run not found";
        }
        
        model.addAttribute("testRun", testRun.get());
        return "test-run-details";
    }
    
    @PostMapping("/specs/{id}/generate-tests")
    public String generateTests(@PathVariable Long id) {
        try {
            apiQaService.generateFeatureFiles(id);
            return "redirect:/specs/" + id + "?success=Tests generated successfully";
        } catch (Exception e) {
            return "redirect:/specs/" + id + "?error=Failed to generate tests: " + e.getMessage();
        }
    }
    
    @PostMapping("/specs/{id}/run-tests")
    public String runTests(@PathVariable Long id, @RequestParam String runName) {
        try {
            apiQaService.executeTestRun(id, runName, com.apiqa.model.TestRunType.MANUAL);
            return "redirect:/specs/" + id + "?success=Test run started successfully";
        } catch (Exception e) {
            return "redirect:/specs/" + id + "?error=Failed to start test run: " + e.getMessage();
        }
    }
    
    @PostMapping("/specs/{id}/run-tests-by-suite")
    public String runTestsBySuite(@PathVariable Long id, @RequestParam String runName, @RequestParam String suiteType) {
        try {
            com.apiqa.model.TestSuiteType suiteTypeEnum = com.apiqa.model.TestSuiteType.valueOf(suiteType);
            apiQaService.executeTestRunBySuiteType(id, runName, com.apiqa.model.TestRunType.MANUAL, suiteTypeEnum);
            return "redirect:/specs/" + id + "?success=" + suiteType + " test run started successfully";
        } catch (Exception e) {
            return "redirect:/specs/" + id + "?error=Failed to start " + suiteType + " test run: " + e.getMessage();
        }
    }
    
    @PostMapping("/test-runs/{id}/retry")
    public String retryFailedTests(@PathVariable Long id) {
        try {
            apiQaService.retryFailedTests(id);
            return "redirect:/test-runs/" + id + "?success=Retry started successfully";
        } catch (Exception e) {
            return "redirect:/test-runs/" + id + "?error=Failed to retry tests: " + e.getMessage();
        }
    }
    
    @PostMapping("/specs/{id}/delete")
    public String deleteSpec(@PathVariable Long id) {
        try {
            apiQaService.deleteApiSpec(id);
            return "redirect:/specs?success=API Spec deleted successfully";
        } catch (Exception e) {
            return "redirect:/specs?error=Failed to delete API Spec: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/test-executions/{id}")
    @ResponseBody
    public ResponseEntity<TestExecutionDetailsDto> getTestExecutionDetails(@PathVariable Long id) {
        try {
            Optional<TestExecution> execution = testExecutionService.getTestExecutionById(id);
            if (execution.isPresent()) {
                TestExecution exec = execution.get();
                TestExecutionDetailsDto dto = new TestExecutionDetailsDto(
                    exec.getId(),
                    exec.getStatus(),
                    exec.getExecutedAt(),
                    exec.getExecutionTimeMs(),
                    exec.getActualStatusCode(),
                    exec.getActualResponseBody(),
                    exec.getActualHeaders(),
                    exec.getErrorMessage(),
                    exec.getValidationResults(),
                    exec.getRequestUrl(),
                    exec.getRequestMethod(),
                    exec.getRequestHeaders(),
                    exec.getRequestBody(),
                    exec.getTestRunId(),
                    exec.getTestScenario() != null ? exec.getTestScenario().getId() : null
                );
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/api/scheduled-tests/run")
    @ResponseBody
    public ResponseEntity<String> runScheduledTestsManually() {
        try {
            scheduledTestService.runScheduledTestsManually();
            return ResponseEntity.ok("Scheduled tests executed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to run scheduled tests: " + e.getMessage());
        }
    }
    
    @PostMapping("/test-runs/{id}/send-email")
    public String sendTestRunEmail(@PathVariable Long id, @RequestParam String toEmail) {
        try {
            Optional<TestRun> testRunOpt = apiQaService.getTestRunById(id);
            if (testRunOpt.isEmpty()) {
                return "redirect:/test-runs/" + id + "?error=Test run not found";
            }
            
            boolean success = emailService.sendTestRunReport(toEmail, testRunOpt.get());
            if (success) {
                return "redirect:/test-runs/" + id + "?success=Test run report sent successfully to " + toEmail;
            } else {
                return "redirect:/test-runs/" + id + "?error=Failed to send email. Please check the email address and try again.";
            }
        } catch (Exception e) {
            return "redirect:/test-runs/" + id + "?error=Failed to send email: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/feature-content/{id}")
    @ResponseBody
    public ResponseEntity<String> getFeatureContent(@PathVariable Long id) {
        try {
            Optional<FeatureFile> featureOpt = apiQaService.getFeatureFileById(id);
            if (featureOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FeatureFile feature = featureOpt.get();
            String content = feature.getContent();
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.ok("No content available for this feature file.");
            }
            
            return ResponseEntity.ok(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving feature content: " + e.getMessage());
        }
    }
    
    @PutMapping("/api/feature-content/{id}")
    @ResponseBody
    public ResponseEntity<String> updateFeatureContent(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Content cannot be empty");
            }
            
            Optional<FeatureFile> featureOpt = apiQaService.getFeatureFileById(id);
            if (featureOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            FeatureFile feature = featureOpt.get();
            feature.setContent(content);
            apiQaService.saveFeatureFile(feature);
            
            return ResponseEntity.ok("Feature file updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating feature content: " + e.getMessage());
        }
    }
}
