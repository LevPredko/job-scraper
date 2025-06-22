package com.example.jobscraper.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String positionName;

    @Column(length = 1000)
    private String jobUrl;

    private String organizationUrl;

    @Column(length = 1000)
    private String logoUrl;

    private String organizationTitle;

    private String laborFunction;

    @ElementCollection
    private List<String> locations;

    private Instant postedDate;

    @Lob
    private String description;

    @ElementCollection
    private List<String> tags;
}
