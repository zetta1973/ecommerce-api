# Multi-stage build for Spring Boot with Undertow
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Add health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Health check for Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# Use exec form for proper signal handling
ENTRYPOINT ["java", "-jar", "app.jar"]
