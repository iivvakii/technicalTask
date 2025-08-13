# **Project: Technical Task - Job Scraper**

## Description

This project is a web scraper that extracts job postings from Techstars and similar platforms.
The application stores scraped jobs in a PostgreSQL database and can generate a SQL dump of all results.
Additionally, it can optionally upload the scraped data to Google Sheets using the Google API.

## Features of the Scraping System

- **Multi-level data extraction**: Initially, basic job information is collected from the job list (position, link, tags). Then, a detailed parsing is performed for each job.

- **Handling different domains:**
If a job link does not contain the main domain (techstars.com), the system stores only basic data:

  - _job title_

  - _job URL_

  - _tags_
   
  Detailed data is not collected for such jobs.

- **Retry mechanism and fallback:**
Each job is attempted 3 times using Jsoup.
If all attempts fail, a fallback using Selenium is triggered to bypass server restrictions or GZIP-related issues.

- **Multithreading:**
A thread pool (ExecutorService) is used to speed up data collection. The number of threads is configurable via application.properties.

- **Logging:**
The system logs in detail:

  - number of jobs processed

  - retry attempts

  - fallback usage

  - parsing errors

- **SQL dump:**
All scraped jobs are saved to an SQL file (jobs_dump.sql) along with the database schema.

- **Limitations:**
If a job cannot be processed even with fallback, it is skipped to avoid blocking the entire process.

### API Endpoint

`GET /jobs`

You can pass query parameters for filtering jobs by job_functions:

#### Request example:

Endpoint: `POST /jobs`

Content-Type: `application/json`


#### Job Functions

When sending a POST request to /jobs, you can specify one or more job functions. Valid values are

- ACCOUNTING ("Accounting & Finance")
- ADMINISTRATION ("Administration")
- COMPLIANCE_REGULATORY ("Compliance / Regulatory")
- CUSTOMER_SERVICE ("Customer Service")
- DATA_SCIENCE ("Data Science")
- DESIGN ("Design")
- IT ("IT")
- LEGAL ("Legal")
- MARKETING ("Marketing & Communications")
- OPERATIONS ("Operations")
- OTHER_ENGINEERING ("Other Engineering")
- PEOPLE_HR ("People & HR")
- PRODUCT ("Product")
- QUALITY_ASSURANCE ("Quality Assurance")
- SALES_BUSINESS_DEVELOPMENT ("Sales & Business Development")
- SOFTWARE_ENGINEERING ("Software Engineering")

**Request body:**

_{
    "jobFunctions": ["IT", "SOFTWARE_ENGINEERING"]
}_

