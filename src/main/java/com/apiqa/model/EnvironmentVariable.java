package com.apiqa.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "environment_variables")
public class EnvironmentVariable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "`key`")
    private String key;
    
    @Column(columnDefinition = "TEXT", name = "`value`")
    private String value;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false)
    @JsonIgnore
    private Environment environment;
    
    @Column(nullable = false)
    private String variableType = "STRING"; // STRING, NUMBER, BOOLEAN, URL
    
    @Column(nullable = false)
    private Boolean isSensitive = false; // For passwords, tokens, etc.
    
    // Constructors
    public EnvironmentVariable() {}
    
    public EnvironmentVariable(String key, String value, String description, Environment environment) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.environment = environment;
        this.variableType = "STRING";
        this.isSensitive = false;
    }
    
    public EnvironmentVariable(String key, String value, String description, Environment environment, String variableType, Boolean isSensitive) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.environment = environment;
        this.variableType = variableType != null ? variableType : "STRING";
        this.isSensitive = isSensitive != null ? isSensitive : false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Environment getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    
    public String getVariableType() {
        return variableType;
    }
    
    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }
    
    public Boolean getIsSensitive() {
        return isSensitive;
    }
    
    public void setIsSensitive(Boolean isSensitive) {
        this.isSensitive = isSensitive;
    }
    
    @Override
    public String toString() {
        return "EnvironmentVariable{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", value='" + (isSensitive ? "***" : value) + '\'' +
                ", description='" + description + '\'' +
                ", variableType='" + variableType + '\'' +
                ", isSensitive=" + isSensitive +
                '}';
    }
}
