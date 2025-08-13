# Використовуємо офіційний JDK 17
FROM eclipse-temurin:17-jdk-alpine

# Робоча директорія
WORKDIR /app

# Копіюємо jar-файл додатку
COPY target/*.jar app.jar

# Відкриваємо порт для Spring Boot
EXPOSE 8080

# Запуск додатку
ENTRYPOINT ["java", "-jar", "app.jar"]