package com.apiqa.service;

import com.apiqa.model.ApiSpec;
import com.apiqa.model.TestRun;
import com.apiqa.model.TestRunType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class ScheduledTestService {
    
    @Autowired
    private ApiQaService apiQaService;
    
    /**
     * Run all available tests for each API spec every 3 minutes
     * Cron expression: every 4 hours at 0 minutes
     */
    @Scheduled(cron = "0 0 */4 * * *")  // Every 4 hours at 0 minutes
    public void runScheduledTests() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println("=== Starting Scheduled Test Run at " + timestamp + " ===");
            
            // Get all API specs
            List<ApiSpec> apiSpecs = apiQaService.getAllApiSpecs();
            
            if (apiSpecs.isEmpty()) {
                System.out.println("No API specs found for scheduled testing.");
                return;
            }
            
            System.out.println("Found " + apiSpecs.size() + " API spec(s) for scheduled testing.");
            
            int totalRuns = 0;
            int successfulRuns = 0;
            int failedRuns = 0;
            
            // Run tests for each API spec
            for (ApiSpec apiSpec : apiSpecs) {
                try {
                    System.out.println("Running scheduled tests for API Spec: " + apiSpec.getName() + " (ID: " + apiSpec.getId() + ")");
                    
                    // Check if the API spec has any feature files (tests)
                    // Force initialization of the collection within the transaction
                    int featureFileCount = apiSpec.getFeatureFiles().size();
                    if (featureFileCount == 0) {
                        System.out.println("  - No tests available for API Spec: " + apiSpec.getName());
                        continue;
                    }
                    
                    // Generate a unique run name with timestamp
                    String runName = "Scheduled Run - " + timestamp;
                    
                    // Execute the test run
                    TestRun testRun = apiQaService.executeTestRun(apiSpec.getId(), runName, TestRunType.SCHEDULED);
                    
                    totalRuns++;
                    if (testRun.getStatus().name().equals("COMPLETED")) {
                        successfulRuns++;
                        System.out.println("  ✓ Scheduled test run completed successfully for: " + apiSpec.getName());
                    } else {
                        failedRuns++;
                        System.out.println("  ✗ Scheduled test run failed for: " + apiSpec.getName() + 
                            " (Status: " + testRun.getStatus() + ")");
                    }
                    
                } catch (Exception e) {
                    failedRuns++;
                    System.err.println("  ✗ Error running scheduled tests for API Spec: " + apiSpec.getName() + 
                        " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("=== Scheduled Test Run Summary ===");
            System.out.println("Total API Specs: " + apiSpecs.size());
            System.out.println("Test Runs Executed: " + totalRuns);
            System.out.println("Successful Runs: " + successfulRuns);
            System.out.println("Failed Runs: " + failedRuns);
            System.out.println("=== Scheduled Test Run Completed ===\n");
            
        } catch (Exception e) {
            System.err.println("Error in scheduled test execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Manual trigger for scheduled tests (for testing purposes)
     */
    public void runScheduledTestsManually() {
        System.out.println("Manual trigger for scheduled tests...");
        runScheduledTests();
    }
}