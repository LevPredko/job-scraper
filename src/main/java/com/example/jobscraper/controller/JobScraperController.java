package com.example.jobscraper.controller;

import com.example.jobscraper.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scrape")
public class JobScraperController {

    private final JobScraperService scraperService;

    @PostMapping
    public String scrape(@RequestParam String function) {
        try {
            scraperService.scrapeJobsByFunction(function);
            return "Scraping done!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
