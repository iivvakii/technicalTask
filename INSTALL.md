**Project: Technical Task - Job Scraper**
**1. Prerequisites**
   
Before you begin, make sure you have the following installed:
- Java 17 JDK
- Maven
- Docker
- Docker Compose


Ensure ports 8080 (app) and 4444 (Selenium) are free.

(Optional) Install ChromeDriver if you want to run locally without Docker.

**2. Clone the repository**
   
   `git clone https://github.com/your-username/technical-task.git`
   
**3. Build the Spring Boot application**
   
 `./mvnw clean package`
   
This will generate the target/*.jar file required for Docker.

**4. Run with Docker Compose**

   Build and start the containers:

 `docker-compose up --build`

Spring Boot app will be available at: http://localhost:8080

PostgreSQL database will be available at: localhost:5432
Credentials:

- Username: postgres

- Password: password

- Database: mydb

To stop the containers:

 `docker-compose down`

**5. Database dump**

The application generates files during scraping:

SQL dump file (Software Engineering jobs.sql)

Stored in the host machine project root (or the path you specify in SqlGeneratorService).

If you run in Docker, make sure the path is accessible from inside the container. For example, you can create a volume:


- volumes:
   - ./data:/data


  and write files to /app/data/Software Engineering jobs.sql inside the container.

**6. Stopping the project**

`docker-compose down`

**7. Running locally (optional)**

If you prefer to run the app locally without Docker:

Install Chrome and ChromeDriver.

Set system property in your code:

`System.setProperty("webdriver.chrome.driver", "your/path/to/webdriver");`

Run the Spring Boot app normally (./mvnw spring-boot:run or from IDE).