package com.example.technicaltask.service;

import com.example.technicaltask.model.Job;
import com.example.technicaltask.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SqlGeneratorService {

    public static final String CREATE_JOB_TABLE = """
        CREATE TABLE jobs (
            id BIGSERIAL PRIMARY KEY,
            position_name TEXT,
            job_url TEXT UNIQUE,
            organization_url TEXT,
            logo_url TEXT,
            organization_title TEXT,
            labor_function TEXT,
            posted_date BIGINT,
            description_html TEXT
        );
        """;

    public static final String CREATE_JOB_LOCATION_TABLE = """
        CREATE TABLE job_locations (
            job_id BIGINT,
            location TEXT,
            FOREIGN KEY (job_id) REFERENCES jobs(id)
        );
        """;


    public static final String CREATE_TAGS_TABLE = """
        CREATE TABLE tags (
            id BIGSERIAL PRIMARY KEY,
            name TEXT
        );
        """;

    public static final String CREATE_JOB_TAGS_TABLE = """
        CREATE TABLE job_tags (
            job_id BIGINT,
            tag_id BIGINT,
            PRIMARY KEY (job_id, tag_id),
            FOREIGN KEY (job_id) REFERENCES jobs(id),
            FOREIGN KEY (tag_id) REFERENCES tags(id)
        );
        """;

    public static final String INSERT_JOB_VALUE = "INSERT INTO jobs (id, position_name, job_url, organization_url, logo_url, organization_title, labor_function, posted_date, description_html) " +
            "VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %d, '%s');\n";

    public static final String INSERT_JOB_TAGS_VALUE = "INSERT INTO job_tags (job_id, tag_id) VALUES (%d, %d);\n";

    public static final String INSERT_TAGS_VALUE = "INSERT INTO tags (id, name) VALUES (%d, '%s');\n";

    public static final String INSERT_LOCATION_VALUE = "INSERT INTO job_locations (job_id, location) VALUES (%d, '%s');\n";


    public void dumpJobsToSql(List<Job> jobs, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append(CREATE_JOB_TABLE);
        sb.append(CREATE_JOB_LOCATION_TABLE);
        sb.append(CREATE_TAGS_TABLE);
        sb.append(CREATE_JOB_TAGS_TABLE);

        // INSERT INTO jobs
        long idCounter = 1;
        Map<String, Long> tagMap = new HashMap<>();
        long tagIdCounter = 1;

        for (Job job : jobs) {
            sb.append(String.format(
                    INSERT_JOB_VALUE,
                    idCounter,
                    escape(job.getPositionName()),
                    escape(job.getJobUrl()),
                    escape(job.getOrganizationUrl()),
                    escape(job.getLogoUrl()),
                    escape(job.getOrganizationTitle()),
                    escape(job.getLaborFunction()),
                    job.getPostedDate() != null ? job.getPostedDate() : 0,
                    escape(job.getDescriptionHtml())
            ));

            for (String location : job.getLocations()) {
                sb.append(String.format(
                        INSERT_LOCATION_VALUE,
                        idCounter, escape(location)
                ));
            }

            for (Tag tag : job.getTags()) {
                if (!tagMap.containsKey(tag.getName())) {
                    sb.append(String.format(
                            INSERT_TAGS_VALUE,
                            tagIdCounter, escape(tag.getName())
                    ));
                    tagMap.put(tag.getName(), tagIdCounter++);
                }
                sb.append(String.format(
                        INSERT_JOB_TAGS_VALUE,
                        idCounter, tagMap.get(tag.getName())
                ));
            }

            idCounter++;
        }

        Files.writeString(Path.of(filePath), sb.toString(), StandardCharsets.UTF_8);
    }

    private String escape(String val) {
        return val == null ? "" : val.replace("'", "''");
    }
}
