package com.example.technicaltask.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String positionName;
    @Column(unique = true, columnDefinition = "TEXT")
    private String jobUrl;
    @Column(columnDefinition = "TEXT")
    private String organizationUrl;
    @Column(columnDefinition = "TEXT")
    private String logoUrl;
    @Column(columnDefinition = "TEXT")
    private String organizationTitle;
    @Column(columnDefinition = "TEXT")
    private String laborFunction;

    @ElementCollection
    @CollectionTable(name = "job_locations", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "location", columnDefinition = "TEXT")
    private List<String> locations = new ArrayList<>();

    private Long postedDate;

    @Column(columnDefinition = "TEXT")
    private String descriptionHtml;

    @ManyToMany
    @JoinTable(
            name = "job_tags",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
}
