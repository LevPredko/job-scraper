package com.example.jobscraper.service;

import com.example.jobscraper.model.Job;
import com.example.jobscraper.model.Location;
import com.example.jobscraper.model.Tag;
import com.example.jobscraper.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SqlDumpService {

    private final JobRepository jobRepository;

    public void dumpToSql(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {

            writer.write("""
                    -- Database creation schema

                    CREATE TABLE job (
                        id BIGSERIAL PRIMARY KEY,
                        position_name TEXT,
                        job_url TEXT,
                        organization_url TEXT,
                        logo_url TEXT,
                        organization_title TEXT,
                        labor_function TEXT,
                        posted_date TIMESTAMP,
                        description TEXT
                    );

                    CREATE TABLE tag (
                        id BIGSERIAL PRIMARY KEY,
                        name TEXT UNIQUE
                    );

                    CREATE TABLE location (
                        id BIGSERIAL PRIMARY KEY,
                        city TEXT,
                        region TEXT,
                        country TEXT,
                        job_id BIGINT REFERENCES job(id)
                    );

                    CREATE TABLE job_tags (
                        job_id BIGINT REFERENCES job(id),
                        tag_id BIGINT REFERENCES tag(id),
                        PRIMARY KEY (job_id, tag_id)
                    );

                    -- Data inserts

                    """);

            Set<Long> writtenTags = new HashSet<>();

            for (Job job : jobRepository.findAll()) {

                writer.write(String.format("""
                        INSERT INTO job (id, position_name, job_url, organization_url, logo_url, organization_title, labor_function, posted_date, description)
                        VALUES (%d, %s, %s, %s, %s, %s, %s, '%s', %s);
                        """,
                        job.getId(),
                        escape(job.getPositionName()),
                        escape(job.getJobUrl()),
                        escape(job.getOrganizationUrl()),
                        escape(job.getLogoUrl()),
                        escape(job.getOrganizationTitle()),
                        escape(job.getLaborFunction()),
                        job.getPostedDate(),
                        escape(job.getDescription())
                ));

                for (Location loc : job.getLocations()) {
                    writer.write(String.format("""
                            INSERT INTO location (city, region, country, job_id)
                            VALUES (%s, %s, %s, %d);
                            """,
                            escape(loc.getCity()),
                            escape(loc.getRegion()),
                            escape(loc.getCountry()),
                            job.getId()
                    ));
                }

                for (Tag tag : job.getTags()) {
                    if (!writtenTags.contains(tag.getId())) {
                        writer.write(String.format("""
                                INSERT INTO tag (id, name)
                                VALUES (%d, %s)
                                ON CONFLICT (id) DO NOTHING;
                                """,
                                tag.getId(), escape(tag.getName())));
                        writtenTags.add(tag.getId());
                    }

                    writer.write(String.format("""
                            INSERT INTO job_tags (job_id, tag_id)
                            VALUES (%d, %d);
                            """,
                            job.getId(), tag.getId()));
                }

                writer.write("\n");
            }

            System.out.println("SQL dump created successfully: " + filePath);

        } catch (IOException e) {
            System.out.println("Error writing SQL dump");
            e.printStackTrace();
        }
    }

    private String escape(String text) {
        if (text == null) return "NULL";
        return "'" + text.replace("'", "''") + "'";
    }
}
