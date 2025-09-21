package com.apiqa.repository;

import com.apiqa.model.ApiSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiSpecRepository extends JpaRepository<ApiSpec, Long> {
    
    List<ApiSpec> findByNameContainingIgnoreCase(String name);
    
    List<ApiSpec> findByUploadedBy(String uploadedBy);
    
    @Query("SELECT a FROM ApiSpec a ORDER BY a.uploadedAt DESC")
    List<ApiSpec> findAllOrderByUploadedAtDesc();
    
    @Query("SELECT a FROM ApiSpec a WHERE a.name = :name AND a.version = :version")
    Optional<ApiSpec> findByNameAndVersion(@Param("name") String name, @Param("version") String version);
    
    @Query("SELECT DISTINCT a FROM ApiSpec a LEFT JOIN FETCH a.featureFiles ORDER BY a.uploadedAt DESC")
    List<ApiSpec> findAllWithFeatureFilesOrderByUploadedAtDesc();
}
