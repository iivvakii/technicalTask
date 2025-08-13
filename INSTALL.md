**Project: Technical Task - Job Scraper**
**1. Prerequisites**
   
Before you begin, make sure you have the following installed:
- Java 17 JDK
- Maven
- Docker
- Docker Compose

**2. Clone the repository**
   
   _git clone https://github.com/your-username/technical-task.git_
   
**3. Build the Spring Boot application**
   
_./mvnw clean package_
   
This will generate the target/*.jar file required for Docker.

**4. Configure environment variables**
   
The application uses environment variables for database connection and other settings.
   You can configure them in docker-compose.yml:

environment:
    
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/mydb
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: password
SPRING_JPA_HIBERNATE_DDL_AUTO: update
JAVA_OPTS: "-Xms512m -Xmx1024m"

**5. Run with Docker Compose**

   Build and start the containers:

_docker-compose up --build_

Spring Boot app will be available at: http://localhost:8080

PostgreSQL database will be available at: localhost:5432
Credentials:

- Username: postgres

- Password: password

- Database: mydb

To stop the containers:

_docker-compose down_

**6. Database dump**
   If you want to generate a SQL dump of scraped jobs:

After scraping, check the configured path in application.properties or JobService
_D:/jobs_dump.sql_

This file will contain CREATE TABLE statements and all job records.

**7. Optional: Run without Docker**

   You can run the Spring Boot app directly with Maven:

_./mvnw spring-boot:run_

Make sure PostgreSQL is running locally and the application.properties points to the correct database.