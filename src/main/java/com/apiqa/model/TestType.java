package com.apiqa.model;

public enum TestType {
    SMOKE("Smoke Tests"),
    REGRESSION("Regression Tests"),
    INTEGRATION("Integration Tests"),
    PERFORMANCE("Performance Tests"),
    SECURITY("Security Tests"),
    FUNCTIONAL("Functional Tests"),
    API_VALIDATION("API Validation Tests"),
    CUSTOM("Custom Tests");
    
    private final String displayName;
    
    TestType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
