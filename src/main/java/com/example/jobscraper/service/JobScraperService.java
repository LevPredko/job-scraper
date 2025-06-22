package com.example.jobscraper.service;

import com.example.jobscraper.model.Job;
import com.example.jobscraper.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobScraperService {

    private final JobRepository jobRepository;

    private static final String BASE_URL = "https://jobs.techstars.com";

    public void scrapeJobsByFunction(String function) throws Exception {
        String url = BASE_URL + "/jobs?function=" + function.replace(" ", "%20");

        Document doc = Jsoup.connect(url).get();

        Elements jobCards = doc.select(".job-listing .job");

        for (Element jobCard : jobCards) {
            String jobUrl = BASE_URL + jobCard.select("a").attr("href");

            if (isRedirectToExternal(jobUrl)) continue;

            if (jobRepository.existsByJobUrl(jobUrl)) continue;

            Document jobPage = Jsoup.connect(jobUrl).get();

            String position = jobPage.select("h1").text();
            String orgUrl = jobPage.select(".organization-details a").attr("href");
            String logo = jobPage.select(".organization-details img").attr("src");
            String orgTitle = jobPage.select(".organization-details h3").text();
            String laborFunction = jobPage.select("dd[data-testid='job-function']").text();
            List<String> tags = jobPage.select(".job-tags span").eachText();

            List<String> locations = new ArrayList<>();
            for (Element el : jobPage.select("dd[data-testid='location'] span")) {
                locations.add(el.text());
            }

            String postedText = jobPage.select("time").attr("datetime");
            Instant postedDate = ZonedDateTime.parse(postedText).toInstant();

            Element desc = jobPage.selectFirst(".job-description");

            Job job = Job.builder()
                    .positionName(position)
                    .jobUrl(jobUrl)
                    .organizationUrl(orgUrl)
                    .logoUrl(logo)
                    .organizationTitle(orgTitle)
                    .laborFunction(laborFunction)
                    .locations(locations)
                    .postedDate(postedDate)
                    .description(desc != null ? desc.html() : "")
                    .tags(tags)
                    .build();

            jobRepository.save(job);
        }
    }

    private boolean isRedirectToExternal(String jobUrl) {
        try {
            String finalUrl = Jsoup.connect(jobUrl)
                    .followRedirects(true)
                    .execute()
                    .url()
                    .toString();
            return !finalUrl.contains("jobs.techstars.com");
        } catch (Exception e) {
            return true;
        }
    }
}
