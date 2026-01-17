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

# Instalamos tzdata para evitar advertencias de zona horaria (opcional pero recomendado)
RUN apk add --no-cache tzdata

# Copiamos el JAR generado desde el build
# Usamos un nombre fijo para evitar problemas si el nombre del JAR cambia
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto estándar de Spring Boot
EXPOSE 8080

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
