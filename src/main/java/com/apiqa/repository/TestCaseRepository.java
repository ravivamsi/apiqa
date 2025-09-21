package com.apiqa.repository;

import com.apiqa.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByTestSuiteId(Long testSuiteId);
    List<TestCase> findByApiSpecId(Long apiSpecId);
    List<TestCase> findByCreatedBy(String createdBy);
    List<TestCase> findByNameContainingIgnoreCase(String name);
}
