package com.apiqa.repository;

import com.apiqa.model.FeatureFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureFileRepository extends JpaRepository<FeatureFile, Long> {
    List<FeatureFile> findByApiSpecId(Long apiSpecId);
}
