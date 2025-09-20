package com.apiqa.model;

public enum TestRunType {
    MANUAL("Manual execution"),
    SCHEDULED("Scheduled execution"),
    RETRY("Retry execution");
    
    private final String description;
    
    TestRunType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
