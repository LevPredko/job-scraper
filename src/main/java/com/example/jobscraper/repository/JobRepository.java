package com.example.jobscraper.repository;

import com.example.jobscraper.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByJobUrl(String jobUrl);
}
