package com.example.jobscraper.service;

import com.example.jobscraper.model.Job;
import com.example.jobscraper.model.Location;
import com.example.jobscraper.model.Tag;
import com.example.jobscraper.repository.JobRepository;
import com.example.jobscraper.repository.TagRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobScraperService {

    private final JobRepository jobRepository;
    private final TagRepository tagRepository;

    private static final String BASE_URL = "https://jobs.techstars.com";

    public void scrapeJobsByFunction(String function) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            String searchUrl = BASE_URL + "/jobs?function=" + function.replace(" ", "%20");
            driver.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            boolean jobsLoaded = false;
            for (int i = 0; i < 9; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(1500);

                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[data-testid='job-title-link']")));
                    jobsLoaded = true;
                    break;
                } catch (TimeoutException ignored) {}
            }

            if (!jobsLoaded) {
                System.out.println("Vacancies did not load after scrolling");
                return;
            }

            List<WebElement> jobLinks = driver.findElements(By.cssSelector("a[data-testid='job-title-link']"));

            int skipped = 0, saved = 0;

            for (WebElement jobLink : jobLinks) {
                String href = jobLink.getAttribute("href");

                if (href == null || !href.startsWith(BASE_URL)) {
                    skipped++;
                    continue;
                }

                if (jobRepository.existsByJobUrl(href)) {
                    continue;
                }

                try {
                    Document jobPage = Jsoup.connect(href).get();

                    String position = getSafeText(jobPage.selectFirst("h1"));
                    String orgUrl = getSafeAttr(jobPage.selectFirst(".organization-details a"), "href");
                    String logo = getSafeAttr(jobPage.selectFirst(".organization-details img"), "src");
                    String orgTitle = getSafeText(jobPage.selectFirst(".organization-details h3"));
                    String laborFunction = getSafeText(jobPage.selectFirst("dd[data-testid='job-function']"));

                    List<String> tagNames = jobPage.select(".job-tags span").eachText();
                    List<String> locationStrings = jobPage.select("dd[data-testid='location'] span").eachText();

                    String postedText = getSafeAttr(jobPage.selectFirst("time"), "datetime");
                    Instant postedDate;
                    if (postedText != null && !postedText.isEmpty()) {
                        try {
                            postedDate = ZonedDateTime.parse(postedText).toInstant();
                        } catch (Exception e) {
                            postedDate = Instant.now();
                        }
                    } else {
                        postedDate = Instant.now();
                    }

                    Element desc = jobPage.selectFirst(".job-description");

                    Job job = Job.builder()
                            .positionName(position)
                            .jobUrl(href)
                            .organizationUrl(orgUrl)
                            .logoUrl(logo)
                            .organizationTitle(orgTitle)
                            .laborFunction(laborFunction)
                            .postedDate(postedDate)
                            .description(desc != null ? desc.html() : "")
                            .build();

                    for (String locStr : locationStrings) {
                        Location location = parseLocation(locStr);
                        location.setJob(job);
                        job.getLocations().add(location);
                    }

                    for (String tagName : tagNames) {
                        Tag tag = findOrCreateTag(tagName);
                        job.getTags().add(tag);
                    }

                    jobRepository.save(job);
                    saved++;

                } catch (Exception ex) {
                    System.out.println("Error parsing vacancy: " + href);
                    ex.printStackTrace();
                }
            }

            System.out.println("Scraping finished. Saved: " + saved + ", Skipped: " + skipped);

        } catch (Exception e) {
            System.out.println("Selenium error");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private Location parseLocation(String locationStr) {
        String[] parts = locationStr.split(",");
        String city = parts.length > 0 ? parts[0].trim() : null;
        String region = parts.length > 1 ? parts[1].trim() : null;
        String country = parts.length > 2 ? parts[2].trim() : null;

        return Location.builder()
                .city(city)
                .region(region)
                .country(country)
                .build();
    }

    private Tag findOrCreateTag(String tagName) {
        Optional<Tag> existingTag = tagRepository.findByName(tagName);
        return existingTag.orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
    }

    private String getSafeText(Element element) {
        return element != null ? element.text() : "";
    }

    private String getSafeAttr(Element element, String attr) {
        return element != null ? element.attr(attr) : "";
    }

    public List<String> getJobFunctions() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(BASE_URL + "/jobs");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("select[data-testid='job-function-select']")));

            WebElement dropdown = driver.findElement(By.cssSelector("select[data-testid='job-function-select']"));
            List<WebElement> optionsList = dropdown.findElements(By.tagName("option"));

            List<String> functions = new ArrayList<>();
            for (WebElement option : optionsList) {
                String value = option.getAttribute("value");
                if (value != null && !value.isEmpty()) {
                    functions.add(value);
                }
            }

            return functions;

        } finally {
            driver.quit();
        }
    }
}
