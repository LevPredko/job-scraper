package com.example.jobscraper.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(length = 1000, unique = true)
    private String jobUrl;

    private String organizationUrl;

    @Column(length = 1000)
    private String logoUrl;

    private String organizationTitle;

    private String laborFunction;

    private Instant postedDate;

    @Lob
    private String description;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "job_tag",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    public void addLocation(Location location) {
        locations.add(location);
        location.setJob(this);
    }

    public void removeLocation(Location location) {
        locations.remove(location);
        location.setJob(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getJobs().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getJobs().remove(this);
    }
}
