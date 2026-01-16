# ----- STAGE 1: Build -----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos solo los archivos necesarios para cachear dependencias
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copiamos el código y construimos
COPY src ./src
RUN mvn -q clean package -DskipTests

# ----- STAGE 2: Run -----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el jar generado desde el build
COPY --from=build /app/target/ecommerce-api-*.jar ecommerce-api.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ecommerce-api.jar"]
