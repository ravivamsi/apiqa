package com.apiqa.repository;

import com.apiqa.model.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {
    
    Optional<Environment> findByName(String name);
    
    List<Environment> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT DISTINCT e FROM Environment e LEFT JOIN FETCH e.variables ORDER BY e.createdAt DESC")
    List<Environment> findAllWithVariablesOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT e FROM Environment e LEFT JOIN FETCH e.variables WHERE e.id = :id")
    Optional<Environment> findByIdWithVariables(Long id);
    
    boolean existsByName(String name);
}
