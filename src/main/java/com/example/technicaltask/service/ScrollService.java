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
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
//        System.setProperty("webdriver.chrome.driver", "D:/chromedriver-win64/chromedriver-win64/chromedriver.exe");
        WebDriver driver = null;
        try {
//            driver = new ChromeDriver();
            URL gridUrl = new URL("http://selenium:4444/wd/hub");
            ChromeOptions options = getChromeOptions();

            driver = new RemoteWebDriver(gridUrl, options);

            driver.get(buildTechstarsUrl(jobFunctions));
            Thread.sleep(1000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            JavascriptExecutor js = (JavascriptExecutor) driver;

            closeButtonCookie(wait);

            showMoreButton(wait);

            long lastHeight = (long) js.executeScript("return document.body.scrollHeight");

            while (true) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(1000);

                long newHeight = (long) js.executeScript("return document.body.scrollHeight");
                if (newHeight == lastHeight) {
                    break;
                }
                lastHeight = newHeight;
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

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.7204.183 Safari/537.36");
        return options;
    }

    private void showMoreButton(WebDriverWait wait) throws InterruptedException {
        try {
            WebElement showMoreButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id='content']/div[2]/div[2]/div[3]/button")));
            showMoreButton.click();
            log.info("Clicked 'Show more' button");
            Thread.sleep(2000);
        } catch (TimeoutException e) {
            log.info("No 'Show more' button found, continue scrolling");
        }
    }

    private void closeButtonCookie(WebDriverWait wait) {
        try {
            WebElement cookieButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@id=\"onetrust-close-btn-container\"]/button")));
            cookieButton.click();
            log.info("Cookie banner closed");
        } catch (TimeoutException e) {
            log.info("No cookie banner found, continue scrolling");
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
