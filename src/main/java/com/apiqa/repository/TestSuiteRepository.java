package com.apiqa.repository;

import com.apiqa.model.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    List<TestSuite> findByCreatedBy(String createdBy);
    List<TestSuite> findByNameContainingIgnoreCase(String name);
}
