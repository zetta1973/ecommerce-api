# CI/CD Pipeline Documentation

This document provides an overview of the current CI/CD pipeline for the Ecommerce API project, including testing, SonarCloud analysis, Docker image building, and deployment to GitHub Container Registry.

## Pipeline Overview

The CI/CD pipeline is configured in `.github/workflows/ci-cd-kind.yml` and runs on the following events:
- **Pushes** to `main`, `develop`, and `feature/**` branches
- **Pull requests** targeting `main` and `develop` branches

## Pipeline Stages

### 1. Checkout Code
```yaml
- name: Checkout code
  uses: actions/checkout@v4
  with:
    fetch-depth: 0  # Fetch full history for SonarCloud
```
Checks out the repository code with complete Git history for SonarCloud analysis.

### 2. Set Up JDK 17
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
```
Configures Java 17 environment using Eclipse Temurin distribution and caches Maven dependencies.

### 3. Clean Maven Cache
```yaml
- name: Clean Maven cache
  run: mvn dependency:purge-local-repository
```
Cleans the local Maven repository to ensure fresh builds.

### 4. Run Tests and Generate Coverage
```yaml
- name: Run tests and generate coverage
  run: mvn clean verify jacoco:report
```
Executes all unit and integration tests using Maven and generates JaCoCo coverage reports.

### 5. SonarCloud Analysis
```yaml
- name: SonarCloud Scan
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=zetta1973_ecommerce_api -Dsonar.organization=zetta1973 -Dsonar.host.url=https://sonarcloud.io
```

**Purpose**: Performs static code analysis using SonarCloud.

**Configuration**:
- **SONAR_TOKEN**: Secret token for SonarCloud authentication
- **Project Key**: `zetta1973_ecommerce_api`
- **Organization**: `zetta1973`
- **Host**: `https://sonarcloud.io`

**SonarCloud Configuration**:
The project is configured in `sonar-project.properties`:
```properties
sonar.projectKey=zetta1973_ecommerce_api
sonar.projectName=Ecommerce API
sonar.organization=zetta1973
sonar.host.url=https://sonarcloud.io
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

### 6. Build JAR
```yaml
- name: Build JAR
  run: mvn clean package -DskipTests
```
Builds the project and packages it into an executable Spring Boot JAR file.

### 7. Verify JAR Exists
```yaml
- name: Verify JAR exists
  run: ls -la target/*.jar
```
Verifies that the JAR file was successfully created.

### 8. Build Docker Image
```yaml
- name: Build Docker image
  run: docker build -t ecommerce-api:ci --cache-from=ghcr.io/${{ github.repository }}/ecommerce-api:ci --cache-to=type=gha,mode=max .
```

**Purpose**: Builds a Docker image for the application.

**Configuration**:
- **Image Tag**: `ecommerce-api:ci`
- **Cache From**: Uses cached layers from GitHub Container Registry
- **Cache To**: Saves layers to GitHub Actions cache for future builds

### 9. Verify Docker Image
```yaml
- name: Verify Docker image
  run: docker images | grep ecommerce-api
```
Verifies that the Docker image was successfully built.

### 10. Login to GitHub Container Registry
```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```
Authenticates with GitHub Container Registry using the GITHUB_TOKEN.

### 11. Tag and Push Docker Image to GHCR
```yaml
- name: Tag and push Docker image to GHCR
  run: |
    IMAGE_TAG=ghcr.io/${{ github.repository }}/ecommerce-api:ci-${{ github.sha }}
    docker tag ecommerce-api:ci $IMAGE_TAG
    docker push $IMAGE_TAG
    echo "Image pushed to: $IMAGE_TAG"
```

**Purpose**: Tags and pushes the Docker image to GitHub Container Registry.

**Configuration**:
- **Image Tag**: `ghcr.io/{repository}/ecommerce-api:ci-{commit-sha}`
- **Example**: `ghcr.io/zetta1973/ecommerce-api:ci-1a2b3c4d5e6f789`

### 12. Save and Upload Docker Image Artifact
```yaml
- name: Save Docker image as artifact (optional)
  run: docker save -o ecommerce-api.tar ecommerce-api:ci

- name: Upload Docker image artifact
  uses: actions/upload-artifact@v4
  with:
    name: ecommerce-api-docker-image
    path: ecommerce-api.tar
    retention-days: 7
```

**Purpose**: Saves the Docker image as a downloadable artifact for manual inspection or deployment.

## Required Secrets

The following secrets must be configured in GitHub Secrets (`Settings` > `Secrets and variables` > `Actions`):

### 1. SONAR_TOKEN
- **Purpose**: Authenticates with SonarCloud for static code analysis
- **How to Obtain**: Generate a token in SonarCloud (My Account > Security)
- **Required**: Yes

## Build Artifacts

The following artifacts are generated for each build:

1. **Test Results**: Maven surefire reports
2. **Coverage Report**: JaCoCo XML and HTML reports
3. **Docker Image**: `ecommerce-api-docker-image` (Docker image as .tar file)
4. **SonarCloud Analysis**: Code quality metrics in SonarCloud dashboard

## Quality Gates

The pipeline enforces the following quality gates:

1. **Tests**: All tests must pass
2. **Code Coverage**: Generated and analyzed by JaCoCo
3. **SonarCloud**: Quality gate must pass
4. **Build**: Maven build and Docker build must succeed

## Performance Optimizations

### Maven Caching
- Maven dependencies are cached between builds using GitHub Actions cache
- Reduces dependency download time

### Docker Layer Caching
- Uses GitHub Container Registry for layer caching
- GitHub Actions cache for build context
- Significantly reduces Docker build time

### Parallel Execution
- Steps are optimized for sequential execution where dependencies exist

## Environment Specifications

- **Java Version**: 17 (Temurin)
- **Maven**: Latest compatible version
- **Docker**: Latest stable version
- **Spring Boot**: 3.1.2
- **SonarCloud**: Latest scanner version
- **Runner**: ubuntu-latest

## Security Considerations

### Token Management
- All secrets are stored in GitHub Secrets
- Tokens have minimal required permissions
- Regular rotation recommended

### Dependency Scanning
- Available through SonarCloud analysis
- No additional OWASP scanning (removed due to H2 database issues)

### Container Security
- Base image scanning recommended before production deployment
- Multi-stage builds for minimal attack surface

## Monitoring and Observability

### Build Monitoring
- GitHub Actions provides detailed logs for each step
- Build status visible in repository's Actions tab
- Artifact retention for 7 days

### Code Quality Monitoring
- SonarCloud dashboard provides:
  - Code coverage metrics
  - Technical debt
  - Security hotspots
  - Maintainability issues

## Troubleshooting

### Common Issues and Solutions

1. **SonarCloud Connection Issues**
   - **Error**: Authentication failed or project not found
   - **Solution**: Verify SONAR_TOKEN is correct and project key matches SonarCloud

2. **Docker Build Failures**
   - **Error**: Permission denied or build failures
   - **Solution**: Check Dockerfile syntax and repository permissions

3. **Test Failures**
   - **Error**: Tests failing in CI
   - **Solution**: Review test environment and dependencies

4. **Maven Build Issues**
   - **Error**: Dependency conflicts or compilation failures
   - **Solution**: Clean Maven cache and review dependency versions

## Best Practices

### SonarCloud Integration
- Set up quality gates for code quality standards
- Configure branch analysis for all branches
- Review and fix issues regularly

### Docker Optimization
- Order Dockerfile layers from least to most frequently changing
- Use .dockerignore to exclude unnecessary files
- Leverage multi-stage builds for smaller images

### Maven Optimization
- Use dependency management effectively
- Keep dependency versions updated
- Remove unused dependencies

## Configuration Files

- **Main Workflow**: `.github/workflows/ci-cd-kind.yml`
- **Maven Config**: `pom.xml`
- **SonarCloud Config**: `sonar-project.properties`
- **Docker Config**: `Dockerfile`

## Conclusion

This CI/CD pipeline provides a comprehensive solution for:
- Automated testing with coverage reporting
- Static code analysis and quality gates
- Efficient Docker image building and caching
- Automated deployment to GitHub Container Registry
- Comprehensive monitoring and observability

By following the practices and configurations outlined in this document, you ensure a robust, secure, and efficient CI/CD process for the Ecommerce API project.