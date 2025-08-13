package com.example.technicaltask.service;

import com.example.technicaltask.entity.JobFunctions;
import com.example.technicaltask.entity.JobMapper;
import com.example.technicaltask.exception.JobParsingException;
import com.example.technicaltask.exception.ScrapingException;
import com.example.technicaltask.model.Job;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrollService {
    private final JobMapper jobMapper;

    @Value("${techstars.url}")
    private String url;

    public Document getScroll(List<JobFunctions> jobFunctions) {
        System.setProperty("webdriver.chrome.driver", "D:/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        WebDriver driver = null;
        try {
            driver = new ChromeDriver();
            driver.get(buildTechstarsUrl(jobFunctions));
            Thread.sleep(1000);
            JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
            javascriptExecutor.executeScript("window.scrollBy(0, 1200);");

            WebElement buttonClose = driver.findElement(By.xpath("//*[@id=\"onetrust-close-btn-container\"]/button"));
            buttonClose.click();

            WebElement button = driver.findElement(By.xpath("//*[@id=\"content\"]/div[2]/div[2]/div[3]/button"));
            button.click();

            for (int i = 0; i < 10; i++) {
                javascriptExecutor.executeScript("window.scrollBy(0, 1000);");
                Thread.sleep(1000);
            }

            String pageSource = driver.getPageSource();
            return Jsoup.parse(pageSource, url + "/jobs");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapingException("Page scrolling was interrupted: ", e);
        } catch (Exception e) {
            throw new ScrapingException("Error when loading a page or interacting with the browser: ", e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public String buildTechstarsUrl(List<JobFunctions> jobFunctions) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> jobFunctionsSrt = jobFunctions.stream().map(JobFunctions::getName).toList();

        Map<String, Object> filters = Map.of(
                "job_functions", jobFunctionsSrt
        );
        String json = mapper.writeValueAsString(filters);

        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));

        return url + "/jobs?filter=" + encoded;
    }

    public Job fetchJobWithFallback(Job job) {
        try {
            Document doc = Jsoup.connect(job.getJobUrl())
                    .userAgent("Mozilla/5.0")
                    .timeout(60000)
                    .get();
            return jobMapper.parseToJob(doc, job);
        } catch (IOException e) {
            log.warn("Jsoup failed, using Selenium for: {}", job.getJobUrl());
            return fetchWithSelenium(job);
        }
    }

    private Job fetchWithSelenium(Job job) {
        System.setProperty("webdriver.chrome.driver", "D:/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(job.getJobUrl());
            Thread.sleep(2000);
            String pageSource = driver.getPageSource();
            Document doc = Jsoup.parse(pageSource, job.getJobUrl());
            return jobMapper.parseToJob(doc, job);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobParsingException("Selenium interrupted for job: " + job.getJobUrl(), e);
        } finally {
            driver.quit();
        }
    }
}
