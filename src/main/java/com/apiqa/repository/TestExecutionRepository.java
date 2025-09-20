package com.apiqa.repository;

import com.apiqa.model.TestExecution;
import com.apiqa.model.TestExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionRepository extends JpaRepository<TestExecution, Long> {
    
    List<TestExecution> findByTestRunId(Long testRunId);
    
    List<TestExecution> findByStatus(TestExecutionStatus status);
    
    List<TestExecution> findByTestScenarioId(Long testScenarioId);
    
    @Query("SELECT t FROM TestExecution t WHERE t.testRun.id = :testRunId AND t.status = :status")
    List<TestExecution> findByTestRunIdAndStatus(@Param("testRunId") Long testRunId, @Param("status") TestExecutionStatus status);
    
    @Query("SELECT COUNT(t) FROM TestExecution t WHERE t.testRun.id = :testRunId AND t.status = :status")
    Long countByTestRunIdAndStatus(@Param("testRunId") Long testRunId, @Param("status") TestExecutionStatus status);
}
