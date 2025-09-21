package com.apiqa.repository;

import com.apiqa.model.TestCaseStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseStepRepository extends JpaRepository<TestCaseStep, Long> {
    List<TestCaseStep> findByTestCaseIdOrderByStepOrder(Long testCaseId);
    List<TestCaseStep> findByCreatedBy(String createdBy);
}
