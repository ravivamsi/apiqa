package com.apiqa.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_case_steps")
public class TestCaseStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String stepName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String stepType; // VALIDATE_STATUS_CODE, VALIDATE_RESPONSE_BODY, VALIDATE_RESPONSE_SCHEMA, VALIDATE_HEADERS, EXTRACT_VALUE, ASSERT_VALUE
    
    @Column(columnDefinition = "TEXT")
    private String expectedValue;
    
    @Column(columnDefinition = "TEXT")
    private String jsonPath; // For extracting values from response
    
    @Column(columnDefinition = "TEXT")
    private String assertionType; // EQUALS, CONTAINS, NOT_NULL, NOT_EMPTY, GREATER_THAN, LESS_THAN, REGEX_MATCH
    
    @Column(columnDefinition = "TEXT")
    private String assertionValue;
    
    @Column(nullable = false)
    private Integer stepOrder;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;
    
    // Constructors
    public TestCaseStep() {}
    
    public TestCaseStep(String stepName, String description, String stepType, String expectedValue, 
                       String jsonPath, String assertionType, String assertionValue, Integer stepOrder, 
                       String createdBy, TestCase testCase) {
        this.stepName = stepName;
        this.description = description;
        this.stepType = stepType;
        this.expectedValue = expectedValue;
        this.jsonPath = jsonPath;
        this.assertionType = assertionType;
        this.assertionValue = assertionValue;
        this.stepOrder = stepOrder;
        this.createdBy = createdBy;
        this.testCase = testCase;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStepName() {
        return stepName;
    }
    
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStepType() {
        return stepType;
    }
    
    public void setStepType(String stepType) {
        this.stepType = stepType;
    }
    
    public String getExpectedValue() {
        return expectedValue;
    }
    
    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }
    
    public String getJsonPath() {
        return jsonPath;
    }
    
    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }
    
    public String getAssertionType() {
        return assertionType;
    }
    
    public void setAssertionType(String assertionType) {
        this.assertionType = assertionType;
    }
    
    public String getAssertionValue() {
        return assertionValue;
    }
    
    public void setAssertionValue(String assertionValue) {
        this.assertionValue = assertionValue;
    }
    
    public Integer getStepOrder() {
        return stepOrder;
    }
    
    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public TestCase getTestCase() {
        return testCase;
    }
    
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }
}
