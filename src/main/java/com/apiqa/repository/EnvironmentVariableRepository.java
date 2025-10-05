package com.apiqa.repository;

import com.apiqa.model.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, Long> {
    
    List<EnvironmentVariable> findByEnvironmentIdOrderByKey(Long environmentId);
    
    Optional<EnvironmentVariable> findByEnvironmentIdAndKey(Long environmentId, String key);
    
    boolean existsByEnvironmentIdAndKey(Long environmentId, String key);
    
    @Query("SELECT ev FROM EnvironmentVariable ev WHERE ev.environment.id = :environmentId ORDER BY ev.key")
    List<EnvironmentVariable> findByEnvironmentId(@Param("environmentId") Long environmentId);
    
    void deleteByEnvironmentIdAndKey(Long environmentId, String key);
}
