package com.apiqa.service;

import com.apiqa.model.TestRun;
import com.apiqa.model.TestExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BREVO_API_URL = "https://api.brevo.com/v3/emailCampaigns";
    private final String API_KEY = "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";
    private final String FROM_EMAIL = "hr@rvytech.com";
    private final String FROM_NAME = "Divya N";
    
    public boolean sendTestRunReport(String toEmail, TestRun testRun) {
        try {
            // Validate email format
            if (toEmail == null || !toEmail.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                System.err.println("Invalid email format: " + toEmail);
                return false;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", API_KEY);
            
            Map<String, Object> emailData = new HashMap<>();
            
            // Campaign name
            String campaignName = "Test Report - " + testRun.getRunName();
            if (testRun.getApiSpec() != null) {
                campaignName += " - " + testRun.getApiSpec().getName();
            }
            emailData.put("name", campaignName);
            
            // Subject
            String subject = "Test Run Report - " + testRun.getRunName();
            if (testRun.getApiSpec() != null) {
                subject += " - " + testRun.getApiSpec().getName();
            }
            emailData.put("subject", subject);
            
            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", FROM_NAME);
            sender.put("email", FROM_EMAIL);
            emailData.put("sender", sender);
            
            // Campaign type
            emailData.put("type", "classic");
            
            // HTML content
            String htmlContent = generateHtmlReport(testRun, toEmail);
            emailData.put("htmlContent", htmlContent);
            
            // Recipients - using list ID 2 as specified in the curl command
            Map<String, Object> recipients = new HashMap<>();
            recipients.put("listIds", new int[]{2});
            emailData.put("recipients", recipients);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                BREVO_API_URL, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.err.println("Brevo API Error: " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private String generateTextReport(TestRun testRun, String intendedRecipient) {
        StringBuilder report = new StringBuilder();
        
        report.append("TEST RUN REPORT\n");
        report.append("===============\n\n");
        
        if (intendedRecipient != null) {
            report.append("Report for: ").append(intendedRecipient).append("\n\n");
        }
        
        report.append("Run Name: ").append(testRun.getRunName()).append("\n");
        if (testRun.getApiSpec() != null) {
            report.append("API Spec: ").append(testRun.getApiSpec().getName()).append("\n");
        }
        report.append("Type: ").append(testRun.getRunType().name()).append("\n");
        report.append("Status: ").append(testRun.getStatus().name()).append("\n");
        report.append("Started At: ").append(testRun.getStartedAt() != null ? 
            testRun.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("\n");
        report.append("Completed At: ").append(testRun.getCompletedAt() != null ? 
            testRun.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("\n\n");
        
        if (testRun.getTotalTests() != null) {
            report.append("TEST RESULTS\n");
            report.append("============\n");
            report.append("Total Tests: ").append(testRun.getTotalTests()).append("\n");
            report.append("Passed: ").append(testRun.getPassedTests() != null ? testRun.getPassedTests() : 0).append("\n");
            report.append("Failed: ").append(testRun.getFailedTests() != null ? testRun.getFailedTests() : 0).append("\n");
            report.append("Skipped: ").append(testRun.getSkippedTests() != null ? testRun.getSkippedTests() : 0).append("\n");
            
            if (testRun.getTotalTests() > 0) {
                double passRate = (double) (testRun.getPassedTests() != null ? testRun.getPassedTests() : 0) / testRun.getTotalTests() * 100;
                report.append("Pass Rate: ").append(String.format("%.2f", passRate)).append("%\n");
            }
        }
        
        if (testRun.getTestExecutions() != null && !testRun.getTestExecutions().isEmpty()) {
            report.append("\nTEST EXECUTIONS\n");
            report.append("===============\n");
            
            for (TestExecution execution : testRun.getTestExecutions()) {
                report.append("\nScenario: ").append(execution.getTestScenario() != null ? 
                    execution.getTestScenario().getScenarioName() : "Unknown").append("\n");
                report.append("Method: ").append(execution.getRequestMethod() != null ? 
                    execution.getRequestMethod() : "N/A").append("\n");
                report.append("Endpoint: ").append(execution.getRequestUrl() != null ? 
                    execution.getRequestUrl() : "N/A").append("\n");
                report.append("Status: ").append(execution.getStatus().name()).append("\n");
                report.append("Duration: ").append(execution.getExecutionTimeMs() != null ? 
                    execution.getExecutionTimeMs() + "ms" : "N/A").append("\n");
                
                if (execution.getErrorMessage() != null && !execution.getErrorMessage().isEmpty()) {
                    report.append("Error: ").append(execution.getErrorMessage()).append("\n");
                }
            }
        }
        
        return report.toString();
    }
    
    private String generateHtmlReport(TestRun testRun, String intendedRecipient) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Test Run Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        html.append(".container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        html.append(".header { background: #007bff; color: white; padding: 20px; border-radius: 8px 8px 0 0; margin: -20px -20px 20px -20px; }");
        html.append(".status-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }");
        html.append(".status-completed { background: #28a745; color: white; }");
        html.append(".status-failed { background: #dc3545; color: white; }");
        html.append(".status-running { background: #ffc107; color: black; }");
        html.append(".status-pending { background: #6c757d; color: white; }");
        html.append(".stats { display: flex; justify-content: space-around; margin: 20px 0; }");
        html.append(".stat-item { text-align: center; padding: 15px; border-radius: 8px; }");
        html.append(".stat-passed { background: #d4edda; color: #155724; }");
        html.append(".stat-failed { background: #f8d7da; color: #721c24; }");
        html.append(".stat-skipped { background: #e2e3e5; color: #383d41; }");
        html.append(".stat-total { background: #cce5ff; color: #004085; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("th { background-color: #f8f9fa; font-weight: bold; }");
        html.append("tr:hover { background-color: #f5f5f5; }");
        html.append(".progress-bar { height: 20px; background-color: #e9ecef; border-radius: 10px; overflow: hidden; }");
        html.append(".progress-fill { height: 100%; background-color: #28a745; transition: width 0.3s ease; }");
        html.append(".alert { padding: 15px; margin-bottom: 20px; border: 1px solid transparent; border-radius: 4px; }");
        html.append(".alert-info { color: #0c5460; background-color: #d1ecf1; border-color: #bee5eb; }");
        html.append("</style>");
        html.append("</head><body>");
        
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>Test Run Report</h1>");
        html.append("<p>Generated on ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("</div>");
        
        // Add intended recipient notice
        if (intendedRecipient != null) {
            html.append("<div class='alert alert-info'>");
            html.append("<strong>Report for:</strong> ").append(intendedRecipient);
            html.append("</div>");
        }
        
        // Test Run Info
        html.append("<h2>Run Information</h2>");
        html.append("<table>");
        html.append("<tr><td><strong>Run Name:</strong></td><td>").append(testRun.getRunName()).append("</td></tr>");
        if (testRun.getApiSpec() != null) {
            html.append("<tr><td><strong>API Spec:</strong></td><td>").append(testRun.getApiSpec().getName()).append("</td></tr>");
        }
        html.append("<tr><td><strong>Type:</strong></td><td>").append(testRun.getRunType().name()).append("</td></tr>");
        html.append("<tr><td><strong>Status:</strong></td><td>");
        html.append("<span class='status-badge status-").append(testRun.getStatus().name().toLowerCase()).append("'>");
        html.append(testRun.getStatus().name()).append("</span></td></tr>");
        html.append("<tr><td><strong>Started At:</strong></td><td>");
        html.append(testRun.getStartedAt() != null ? 
            testRun.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("</td></tr>");
        html.append("<tr><td><strong>Completed At:</strong></td><td>");
        html.append(testRun.getCompletedAt() != null ? 
            testRun.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A").append("</td></tr>");
        html.append("</table>");
        
        // Test Results Summary
        if (testRun.getTotalTests() != null) {
            html.append("<h2>Test Results Summary</h2>");
            html.append("<div class='stats'>");
            html.append("<div class='stat-item stat-total'>");
            html.append("<h3>").append(testRun.getTotalTests()).append("</h3>");
            html.append("<p>Total Tests</p></div>");
            html.append("<div class='stat-item stat-passed'>");
            html.append("<h3>").append(testRun.getPassedTests() != null ? testRun.getPassedTests() : 0).append("</h3>");
            html.append("<p>Passed</p></div>");
            html.append("<div class='stat-item stat-failed'>");
            html.append("<h3>").append(testRun.getFailedTests() != null ? testRun.getFailedTests() : 0).append("</h3>");
            html.append("<p>Failed</p></div>");
            html.append("<div class='stat-item stat-skipped'>");
            html.append("<h3>").append(testRun.getSkippedTests() != null ? testRun.getSkippedTests() : 0).append("</h3>");
            html.append("<p>Skipped</p></div>");
            html.append("</div>");
            
            // Pass Rate Progress Bar
            if (testRun.getTotalTests() > 0) {
                double passRate = (double) (testRun.getPassedTests() != null ? testRun.getPassedTests() : 0) / testRun.getTotalTests() * 100;
                html.append("<h3>Pass Rate: ").append(String.format("%.2f", passRate)).append("%</h3>");
                html.append("<div class='progress-bar'>");
                html.append("<div class='progress-fill' style='width: ").append(passRate).append("%'></div>");
                html.append("</div>");
            }
        }
        
        // Test Executions Details
        if (testRun.getTestExecutions() != null && !testRun.getTestExecutions().isEmpty()) {
            html.append("<h2>Test Executions</h2>");
            html.append("<table>");
            html.append("<thead>");
            html.append("<tr><th>Scenario</th><th>Method</th><th>Endpoint</th><th>Status</th><th>Duration</th><th>Error</th></tr>");
            html.append("</thead>");
            html.append("<tbody>");
            
            for (TestExecution execution : testRun.getTestExecutions()) {
                html.append("<tr>");
                html.append("<td>").append(execution.getTestScenario() != null ? 
                    execution.getTestScenario().getScenarioName() : "Unknown").append("</td>");
                html.append("<td>").append(execution.getRequestMethod() != null ? 
                    execution.getRequestMethod() : "N/A").append("</td>");
                html.append("<td>").append(execution.getRequestUrl() != null ? 
                    execution.getRequestUrl() : "N/A").append("</td>");
                html.append("<td><span class='status-badge status-").append(execution.getStatus().name().toLowerCase()).append("'>");
                html.append(execution.getStatus().name()).append("</span></td>");
                html.append("<td>").append(execution.getExecutionTimeMs() != null ? 
                    execution.getExecutionTimeMs() + "ms" : "N/A").append("</td>");
                html.append("<td>").append(execution.getErrorMessage() != null ? 
                    execution.getErrorMessage() : "-").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody>");
            html.append("</table>");
        }
        
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }
}
