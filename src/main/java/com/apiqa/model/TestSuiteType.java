package com.apiqa.model;

public enum TestSuiteType {
    SMOKE("Smoke Tests - Read-only operations"),
    SYSTEM("System/Regression Tests - All CRUD operations"),
    INTEGRATION("Integration/E2E Tests - Endpoint orchestration");
    
    private final String description;
    
    TestSuiteType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
