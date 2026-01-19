# Simple single-stage build for Spring Boot
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy JAR (built locally for CI speed)
COPY target/ecommerce-api-*.jar app.jar

EXPOSE 8080

# Startup with explicit binding and profile
CMD ["java", "-Dserver.address=0.0.0.0", "-Dspring.profiles.active=ci", "-jar", "app.jar"]
