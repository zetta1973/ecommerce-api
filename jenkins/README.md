# Jenkins Configuration

This directory contains Jenkins pipeline and configuration files for the Ecommerce API project.

## Files Structure

```
jenkins/
├── Jenkinsfile              # Pipeline definition
├── pipelines/               # Individual pipeline definitions
│   ├── build-deploy.yml     # Build and deploy pipeline
│   └── test.yml              # Test pipeline
├── scripts/                 # Build and deployment scripts
│   ├── build.sh              # Build application
│   ├── deploy.sh             # Deploy to staging/production
│   └── test.sh               # Run tests
└── config/                  # Configuration files
    ├── staging.properties      # Staging environment config
    └── production.properties   # Production environment config
```

## Usage

1. Configure Jenkins to point to this repository
2. Set up Jenkinsfile for pipeline definition
3. Configure environment-specific properties
4. Set up Jenkins credentials for deployment

## Environment Variables

Required Jenkins environment variables:
- `JAVA_HOME`: Java installation path
- `MAVEN_HOME`: Maven installation path
- `DOCKER_HOST`: Docker registry host
- `SONAR_TOKEN`: SonarCloud authentication token

## Pipeline Stages

1. **Checkout**: Clone source code
2. **Build**: Compile and package application
3. **Test**: Run unit and integration tests
4. **Analyze**: SonarCloud quality analysis
5. **Package**: Create Docker image
6. **Deploy**: Deploy to target environment

## Configuration

Configure Jenkins pipeline-specific settings in:
- `config/staging.properties` - Staging environment
- `config/production.properties` - Production environment

## Scripts

### build.sh
Compiles and packages the application:
```bash
#!/bin/bash
mvn clean package -DskipTests
```

### test.sh
Runs all tests:
```bash
#!/bin/bash
mvn test
```

### deploy.sh
Deploys application to target environment:
```bash
#!/bin/bash
docker push ${IMAGE_NAME}:${VERSION} ${REGISTRY}/${IMAGE_NAME}:${VERSION}
```

## Security

- Store sensitive configuration in Jenkins credentials
- Use Jenkins secret text for passwords and tokens
- Rotate credentials regularly
- Never commit secrets to repository