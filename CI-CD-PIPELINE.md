# CI/CD Pipeline Documentation

This document provides an overview of the CI/CD pipeline for the Ecommerce API project, including SonarCloud analysis, OWASP Dependency Check, Docker image building, and deployment to GitHub Container Registry.

## Pipeline Overview

The CI/CD pipeline is configured in `.github/workflows/ci-cd-kind.yml` and runs on the following events:
- Pushes to `main`, `develop`, and `feature/**` branches
- Pull requests targeting `main` and `develop` branches

## Pipeline Stages

### 1. Checkout Code
```yaml
- name: Checkout code
  uses: actions/checkout@v4
```
Checks out the repository code using the GitHub Actions checkout action.

### 2. Set Up JDK 17
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'
```
Configures the Java 17 environment using the Eclipse Temurin distribution and caches Maven dependencies.

### 3. Run Tests
```yaml
- name: Run tests
  run: mvn test
```
Executes all unit and integration tests using Maven.

### 4. Generate Test Coverage Report
```yaml
- name: Generate test coverage report
  run: mvn jacoco:report
```
Generates a test coverage report using JaCoCo and saves it to `target/site/jacoco/`.

### 5. OWASP Dependency Check
```yaml
- name: OWASP Dependency Check
  run: mvn org.owasp:dependency-check-maven:9.0.9:check -DfailBuildOnCVSS=7 -DskipUpdate=true
```

**Purpose**: Scans project dependencies for known vulnerabilities.

**Configuration**:
- **Goal**: `dependency-check:check` (executes the vulnerability scan)
- **CVSS Threshold**: `7` (fails build on vulnerabilities with CVSS score >= 7)
- **Skip Update**: `true` (uses local database to avoid NVD API issues)

**Output**: Generates a vulnerability report at `target/dependency-check-report.html`.

**Note**: The `skipUpdate` parameter is set to `true` to avoid NVD API authentication errors in CI environments. The plugin uses a local database for vulnerability detection. The NVD API URL and delay are configured in `pom.xml` to ensure compatibility.

### 6. Upload OWASP Report
```yaml
- name: Upload OWASP Report
  uses: actions/upload-artifact@v4
  with:
    name: owasp-report
    path: target/dependency-check-report.html
```
Uploads the OWASP Dependency Check report as a build artifact for review.

### 7. Upload Coverage Report
```yaml
- name: Upload coverage report
  uses: actions/upload-artifact@v4
  with:
    name: coverage-report
    path: target/site/jacoco/
```
Uploads the JaCoCo test coverage report as a build artifact.

### 8. SonarCloud Analysis
```yaml
- name: SonarCloud Analysis
  uses: SonarSource/sonarcloud-github-action@master
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

**Purpose**: Performs static code analysis using SonarCloud.

**Configuration**:
- **GITHUB_TOKEN**: Automatically provided by GitHub Actions for repository access.
- **SONAR_TOKEN**: Secret token for SonarCloud authentication (must be configured in GitHub Secrets).

**Prerequisites**:
1. Create a SonarCloud account at [https://sonarcloud.io](https://sonarcloud.io).
2. Generate a token in SonarCloud (My Account > Security).
3. Add the token as a GitHub secret named `SONAR_TOKEN`.

**SonarCloud Configuration**:
The project is configured in `sonar-project.properties`:
```properties
sonar.projectKey=ecommerce-api
sonar.projectName=Ecommerce API
sonar.organization=zetta1973
sonar.host.url=https://sonarcloud.io
sonar.login=${SONAR_TOKEN}
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.jacoco.reportPath=target/site/jacoco/jacoco.xml
sonar.language=java
sonar.sourceEncoding=UTF-8
```

### 9. Build JAR
```yaml
- name: Build JAR
  run: mvn clean package -DskipTests
```
Builds the project and packages it into a JAR file, skipping tests to save time.

### 10. Verify JAR Exists
```yaml
- name: Verify JAR exists
  run: ls -la target/*.jar
```
Verifies that the JAR file was successfully created.

### 11. Build Docker Image
```yaml
- name: Build Docker image
  run: docker build -t ecommerce-api:ci --cache-from=ghcr.io/${{ github.repository }}/ecommerce-api:ci --cache-to=type=gha,mode=max .
```

**Purpose**: Builds a Docker image for the application.

**Configuration**:
- **Image Tag**: `ecommerce-api:ci`
- **Cache From**: Uses cached layers from GitHub Container Registry for faster builds.
- **Cache To**: Saves layers to GitHub Actions cache for future builds.

**Benefits**:
- **Caching**: Reduces build time by reusing layers from previous builds.
- **Efficiency**: Minimizes resource usage in CI environments.

### 12. Verify Docker Image
```yaml
- name: Verify Docker image
  run: docker images | grep ecommerce-api
```
Verifies that the Docker image was successfully built.

### 13. Login to GitHub Container Registry
```yaml
- name: Login to GitHub Container Registry
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```
Authenticates with GitHub Container Registry using the GITHUB_TOKEN secret.

### 14. Tag and Push Docker Image to GHCR
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
- **Example**: `ghcr.io/your-org/ecommerce-api:ci-1a2b3c4d5e6f789`

**Benefits**:
- **Traceability**: Each image is tagged with the commit SHA for easy tracking.
- **Versioning**: Enables rollback to previous versions if needed.

### 15. Save Docker Image as Artifact
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

**Purpose**: Saves the Docker image as an artifact for manual download if needed.

**Configuration**:
- **Artifact Name**: `ecommerce-api-docker-image`
- **Retention**: 7 days

## Required Secrets

The following secrets must be configured in GitHub Secrets (`Settings` > `Secrets and variables` > `Actions`):

### 1. SONAR_TOKEN
- **Purpose**: Authenticates with SonarCloud for static code analysis.
- **How to Obtain**: Generate a token in SonarCloud (My Account > Security).
- **Note**: This token is specific to your SonarCloud account.

### 2. NVD_API_KEY (Optional)
- **Purpose**: Used by OWASP Dependency Check to update the NVD database.
- **How to Obtain**: Request an API key from [NVD API Key Request](https://nvd.nist.gov/developers/request-an-api-key).
- **Note**: Not required if `skip_update` is set to `true` in the OWASP Dependency Check action.

## Build Artifacts

The following artifacts are generated and uploaded for each build:

1. **OWASP Report**: `owasp-report` (HTML vulnerability report)
2. **Coverage Report**: `coverage-report` (JaCoCo test coverage report)
3. **Docker Image**: `ecommerce-api-docker-image` (Docker image as `.tar` file)

## Failure Conditions

The pipeline fails in the following scenarios:

1. **Test Failures**: If any unit or integration test fails.
2. **High CVSS Vulnerabilities**: If OWASP Dependency Check detects vulnerabilities with CVSS score >= 7.
3. **SonarCloud Issues**: If SonarCloud analysis detects critical issues (configurable in SonarCloud).
4. **Build Failures**: If the Maven build or Docker image build fails.

## Best Practices

### 1. SonarCloud Configuration
- Set up quality gates in SonarCloud to enforce code quality standards.
- Configure branch analysis to monitor all branches.
- Set up notifications for new issues.

### 2. OWASP Dependency Check
- Review the OWASP report regularly to identify and fix vulnerabilities.
- Use the `suppressionFiles` parameter in `pom.xml` to suppress false positives.
- Keep the NVD database updated locally for development environments.

### 3. Docker Caching
- The pipeline uses Docker layer caching to speed up builds.
- Ensure your Dockerfile is optimized for caching (e.g., order layers from least to most frequently changing).

### 4. Security
- Never commit secrets to the repository.
- Use GitHub Secrets for sensitive information.
- Rotate secrets regularly.

## Troubleshooting

### 1. SonarCloud Analysis Failures
- **Error**: `Authentication failed`
  **Solution**: Verify that `SONAR_TOKEN` is correctly configured in GitHub Secrets.

- **Error**: `Project not found`
  **Solution**: Ensure the `sonar.projectKey` in `sonar-project.properties` matches the project key in SonarCloud.

### 2. OWASP Dependency Check Failures
- **Error**: `NVD API returned 403 or 404`
  **Solution**: Set `skip_update: true` in the OWASP Dependency Check action to use the local database.

- **Error**: `No documents exist`
  **Solution**: Ensure the OWASP Dependency Check action has access to the project dependencies.

### 3. Docker Build Failures
- **Error**: `Permission denied`
  **Solution**: Verify that the GitHub token has permissions to push to GitHub Container Registry.

- **Error**: `Cache not found`
  **Solution**: The cache will be created on the first successful build. Subsequent builds will use the cache.

### 4. Test Failures
- **Error**: `Tests failed`
  **Solution**: Review the test output to identify failed tests. Fix the code or update the tests as needed.

## Monitoring and Notifications

- **GitHub Actions**: View build status and logs in the Actions tab of your repository.
- **SonarCloud**: Monitor code quality metrics and issues in the SonarCloud dashboard.
- **OWASP Reports**: Download the OWASP report artifact to review vulnerabilities.

## Conclusion

This CI/CD pipeline provides a comprehensive solution for building, testing, and deploying the Ecommerce API project. It includes:
- Automated testing and code coverage analysis
- Static code analysis with SonarCloud
- Dependency vulnerability scanning with OWASP
- Docker image building and caching
- Deployment to GitHub Container Registry

By following the best practices outlined in this document, you can ensure a robust and secure CI/CD process for your project.
