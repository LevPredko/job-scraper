package com.example.jobscraper.repository;

import com.example.jobscraper.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByJobUrl(String jobUrl);

    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.locations loc " +
            "WHERE (:location IS NULL OR LOWER(loc.city) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "   OR LOWER(loc.region) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "   OR LOWER(loc.country) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "ORDER BY j.postedDate ASC")
    List<Job> findByLocationOrderByPostedDateAsc(@Param("location") String location);

    @Query("SELECT DISTINCT j FROM Job j LEFT JOIN j.locations loc " +
            "WHERE (:location IS NULL OR LOWER(loc.city) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "   OR LOWER(loc.region) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "   OR LOWER(loc.country) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "ORDER BY j.postedDate DESC")
    List<Job> findByLocationOrderByPostedDateDesc(@Param("location") String location);
}
