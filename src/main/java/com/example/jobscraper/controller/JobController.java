package com.example.jobscraper.controller;

import com.example.jobscraper.model.Job;
import com.example.jobscraper.repository.JobRepository;
import com.example.jobscraper.service.JobScraperService;
import com.example.jobscraper.service.SqlDumpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobScraperService jobScraperService;
    private final JobRepository jobRepository;
    private final SqlDumpService sqlDumpService;

    @GetMapping("/functions")
    public List<String> getJobFunctions() {
        return jobScraperService.getJobFunctions();
    }

    @PostMapping("/scrape")
    public String scrapeByFunction(@RequestParam String function) {
        jobScraperService.scrapeJobsByFunction(function);
        return "Scraping started for function: " + function;
    }

    @GetMapping
    public List<Job> getJobs(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        if (sortOrder.equalsIgnoreCase("asc")) {
            return jobRepository.findByLocationOrderByPostedDateAsc(location);
        } else {
            return jobRepository.findByLocationOrderByPostedDateDesc(location);
        }
    }

    @GetMapping("/dump")
    public String dumpSql() {
        String path = "job_dump.sql";
        sqlDumpService.dumpToSql(path);
        return "SQL dump saved to " + path;
    }
}
