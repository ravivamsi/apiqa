package com.apiqa.repository;

import com.apiqa.model.CustomEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomEndpointRepository extends JpaRepository<CustomEndpoint, Long> {
    List<CustomEndpoint> findByTestSuiteId(Long testSuiteId);
    List<CustomEndpoint> findByApiSpecId(Long apiSpecId);
    List<CustomEndpoint> findByCreatedBy(String createdBy);
}
