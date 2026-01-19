# Simple single-stage build for Spring Boot
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy JAR (built locally for CI speed)
COPY target/*.jar app.jar

EXPOSE 8080

# Simple startup
CMD ["java", "-jar", "app.jar"]
