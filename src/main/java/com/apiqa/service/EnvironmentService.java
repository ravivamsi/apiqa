package com.apiqa.service;

import com.apiqa.model.Environment;
import com.apiqa.model.EnvironmentVariable;
import com.apiqa.repository.EnvironmentRepository;
import com.apiqa.repository.EnvironmentVariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnvironmentService {
    
    @Autowired
    private EnvironmentRepository environmentRepository;
    
    @Autowired
    private EnvironmentVariableRepository environmentVariableRepository;
    
    // Environment CRUD operations
    public Environment createEnvironment(String name, String description, String createdByName) {
        if (environmentRepository.existsByName(name)) {
            throw new RuntimeException("Environment with name '" + name + "' already exists");
        }
        
        Environment environment = new Environment(name, description, createdByName);
        return environmentRepository.save(environment);
    }
    
    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAllWithVariablesOrderByCreatedAtDesc();
    }
    
    public Optional<Environment> getEnvironmentById(Long id) {
        return environmentRepository.findByIdWithVariables(id);
    }
    
    public Optional<Environment> getEnvironmentByName(String name) {
        return environmentRepository.findByName(name);
    }
    
    public Environment updateEnvironment(Long id, String name, String description) {
        Optional<Environment> optionalEnvironment = environmentRepository.findById(id);
        if (optionalEnvironment.isEmpty()) {
            throw new RuntimeException("Environment not found with ID: " + id);
        }
        
        Environment environment = optionalEnvironment.get();
        
        // Check if name is being changed and if new name already exists
        if (!environment.getName().equals(name) && environmentRepository.existsByName(name)) {
            throw new RuntimeException("Environment with name '" + name + "' already exists");
        }
        
        environment.setName(name);
        environment.setDescription(description);
        
        return environmentRepository.save(environment);
    }
    
    public void deleteEnvironment(Long id) {
        if (!environmentRepository.existsById(id)) {
            throw new RuntimeException("Environment not found with ID: " + id);
        }
        
        // Delete all variables first
        List<EnvironmentVariable> variables = environmentVariableRepository.findByEnvironmentId(id);
        environmentVariableRepository.deleteAll(variables);
        
        // Delete environment
        environmentRepository.deleteById(id);
    }
    
    // Environment Variable CRUD operations
    public EnvironmentVariable addVariable(Long environmentId, String key, String value, String description, String variableType, Boolean isSensitive) {
        Optional<Environment> optionalEnvironment = environmentRepository.findById(environmentId);
        if (optionalEnvironment.isEmpty()) {
            throw new RuntimeException("Environment not found with ID: " + environmentId);
        }
        
        Environment environment = optionalEnvironment.get();
        
        // Check if variable key already exists in this environment
        if (environmentVariableRepository.existsByEnvironmentIdAndKey(environmentId, key)) {
            throw new RuntimeException("Variable with key '" + key + "' already exists in this environment");
        }
        
        EnvironmentVariable variable = new EnvironmentVariable(key, value, description, environment, variableType, isSensitive);
        return environmentVariableRepository.save(variable);
    }
    
    public List<EnvironmentVariable> getVariablesByEnvironmentId(Long environmentId) {
        return environmentVariableRepository.findByEnvironmentIdOrderByKey(environmentId);
    }
    
    public Optional<EnvironmentVariable> getVariableById(Long id) {
        return environmentVariableRepository.findById(id);
    }
    
    public EnvironmentVariable updateVariable(Long id, String value, String description, String variableType, Boolean isSensitive) {
        Optional<EnvironmentVariable> optionalVariable = environmentVariableRepository.findById(id);
        if (optionalVariable.isEmpty()) {
            throw new RuntimeException("Variable not found with ID: " + id);
        }
        
        EnvironmentVariable variable = optionalVariable.get();
        variable.setValue(value);
        variable.setDescription(description);
        if (variableType != null) {
            variable.setVariableType(variableType);
        }
        if (isSensitive != null) {
            variable.setIsSensitive(isSensitive);
        }
        
        return environmentVariableRepository.save(variable);
    }
    
    public void deleteVariable(Long id) {
        if (!environmentVariableRepository.existsById(id)) {
            throw new RuntimeException("Variable not found with ID: " + id);
        }
        
        environmentVariableRepository.deleteById(id);
    }
    
    public void deleteVariableByKey(Long environmentId, String key) {
        environmentVariableRepository.deleteByEnvironmentIdAndKey(environmentId, key);
    }
    
    public Optional<EnvironmentVariable> getVariableByEnvironmentIdAndKey(Long environmentId, String key) {
        return environmentVariableRepository.findByEnvironmentIdAndKey(environmentId, key);
    }
}
