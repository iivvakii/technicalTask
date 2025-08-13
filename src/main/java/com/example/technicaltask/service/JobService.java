package com.example.technicaltask.service;

import com.example.technicaltask.entity.JobFunctions;
import com.example.technicaltask.entity.JobMapper;
import com.example.technicaltask.exception.JobParsingException;
import com.example.technicaltask.exception.JobProcessingException;
import com.example.technicaltask.exception.ScrapingException;
import com.example.technicaltask.model.Job;
import com.example.technicaltask.model.Tag;
import com.example.technicaltask.repository.JobRepository;
import com.example.technicaltask.repository.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final TagRepository tagRepository;
    private final SqlGeneratorService sqlGeneratorService;
    private final ScrollService scrollService;

    @Value("${threads.pool}")
    private int threads;

    @Value("${techstars.url}")
    private String url;


    public List<Job> getJobs(List<JobFunctions> jobFunctions) {
        log.info("Starting job scraping for {} job functions", jobFunctions.size());
        List<Job> jobs = extractJobs(scrollService.getScroll(jobFunctions));
        List<Job> result = getMoreInfo(jobs);
        log.info("Finished fetching detailed info for {} jobs", result.size());
        try {
            sqlGeneratorService.dumpJobsToSql(result, "D:/jobs_dump.sql");
            log.info("Dumped jobs to SQL file successfully");
        } catch (ScrapingException | IOException e) {
            log.error("Failed to scrape jobs", e);
            throw new JobProcessingException("Failed to scrape jobs", e);
        }
        return result;
    }

    private List<Job> extractJobs(Document doc) {
        List<Job> jobs = new ArrayList<>();
        Elements elements = doc.select("div[data-testid=job-list-item]");
        log.info("Extracting {} job cards from the page", elements.size());

        for (Element jobCard : elements) {
            Element linkEl = jobCard.selectFirst("a[data-testid=read-more]");
            if (linkEl == null) continue;

            String href = linkEl.absUrl("href");
            Job job = new Job();
            job.setJobUrl(href);
            job.setPositionName(Optional.ofNullable(jobCard.selectFirst("div.sc-beqWaB.kToBwF"))
                    .map(Element::text).orElse(null));
            job.setTags(extractTags(jobCard));
            jobs.add(job);
            log.debug("Extracted job card: {}", job.getJobUrl());
        }
        return jobs;
    }

    private List<Tag> extractTags(Element jobCard) {
        return jobCard.select("div[data-testid=tag] div").stream()
                .map(Element::text)
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    return tag;
                })
                .collect(Collectors.toList());
    }



    public List<Job> getMoreInfo(List<Job> jobUrls) {
        log.info("Starting detailed job parsing for {} jobs", jobUrls.size());
        List<Job> modifiedList = new ArrayList<>(jobUrls);
        List<Job> jobs = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CompletionService<Job> completionService = new ExecutorCompletionService<>(executor);


        for (Job job : modifiedList) {
            completionService.submit(() -> {
                log.debug("Processing job: {}", job.getJobUrl());
                if (!job.getJobUrl().contains(url)) {
                    log.warn("Skipping job outside of base URL: {}", job.getJobUrl());
                    return job;
                }
                int retries = 3;
                while (retries > 0) {
                    try {
                        Document doc = Jsoup.connect(job.getJobUrl())
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                                .header("Accept-Encoding", "gzip, deflate")
                                .timeout(60000)
                                .get();
                        log.debug("Successfully parsed job: {}", job.getJobUrl());
                        return jobMapper.parseToJob(doc, job);
                    } catch (IOException e) {
                        retries--;
                        log.warn("Error parsing job details: {}. Retries left: {}", job.getJobUrl(), retries, e);
                        if (retries == 0) {
                            log.error("Failed to parse job after retries: {}", job.getJobUrl(), e);
                            throw new JobParsingException("Failed to parse job: " + job.getJobUrl(), e);
                        }
                        Thread.sleep(1000);
                    }
                }
                return scrollService.fetchJobWithFallback(job);
            });

        }
        for (int i = 0; i < modifiedList.size(); i++) {
            try {
                Job parsedJob = completionService.take().get();
                jobs.add(parsedJob);
                log.info("Parsed {}/{} jobs successfully", i + 1, modifiedList.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while parsing job details", e);
                throw new JobParsingException("Thread interrupted while parsing job details", e);
            } catch (ExecutionException e) {
                log.error("Execution error in job parsing", e.getCause());
            }
        }
        executor.shutdown();
        log.info("Completed detailed parsing for all jobs");
        return jobs;
    }
}
