package com.example.jobscraper.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class DatabaseDumpController {

    @GetMapping("/api/dump")
    public ResponseEntity<Resource> downloadDump() throws IOException, InterruptedException {
        String dumpFileName = "jobs_dump.sql";

        String command = "pg_dump -U postgres -d jobsdb -F p -f " + dumpFileName;

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);

        String dbPassword = System.getenv("DB_PASSWORD");
        pb.environment().put("PGPASSWORD", dbPassword != null ? dbPassword : "");

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            return ResponseEntity.internalServerError().build();
        }

        Path path = Paths.get(dumpFileName);
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + dumpFileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
