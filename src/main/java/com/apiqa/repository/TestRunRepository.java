package com.apiqa.repository;

import com.apiqa.model.TestRun;
import com.apiqa.model.TestRunStatus;
import com.apiqa.model.TestRunType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    
    List<TestRun> findByApiSpecId(Long apiSpecId);
    
    List<TestRun> findByStatus(TestRunStatus status);
    
    List<TestRun> findByRunType(TestRunType runType);
    
    @Query("SELECT t FROM TestRun t WHERE t.apiSpec.id = :apiSpecId ORDER BY t.startedAt DESC")
    List<TestRun> findByApiSpecIdOrderByStartedAtDesc(@Param("apiSpecId") Long apiSpecId);
    
    @Query("SELECT t FROM TestRun t WHERE t.startedAt >= :fromDate AND t.startedAt <= :toDate ORDER BY t.startedAt DESC")
    List<TestRun> findByDateRange(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);
    
    @Query("SELECT COUNT(t) FROM TestRun t WHERE t.apiSpec.id = :apiSpecId AND t.status = :status")
    Long countByApiSpecIdAndStatus(@Param("apiSpecId") Long apiSpecId, @Param("status") TestRunStatus status);
}
