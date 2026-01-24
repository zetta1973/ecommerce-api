# Ecommerce API - Development Infrastructure

This repository contains all necessary infrastructure and configuration files for developing, testing, and deploying the Ecommerce API application.

## 📁 Repository Structure

```
ecommerce-api/
├── 📄 .github/
│   └── workflows/
│       └── ci-cd-kind.yml          # GitHub Actions CI/CD pipeline
├── 🔧 jenkins/                          # Jenkins CI/CD configuration
│   ├── README.md                      # Jenkins setup documentation
│   └── Jenkinsfile                    # Jenkins pipeline definition
├── ☸️  k8s/                            # Kubernetes manifests
│   ├── deployment.yaml                # Main deployment configuration
│   └── application/
│       └── secrets.yaml            # Kubernetes secrets template
├── 📮 postman/                         # API testing collection
│   └── ecommerce-api-collection.json # Complete Postman collection
├── 🔧 scripts/                           # Utility scripts
│   ├── build.sh                     # Build application
│   ├── test.sh                      # Run tests
│   └── deploy.sh                    # Deploy to environments
├── 🐳 Dockerfile                       # Docker configuration
├── 📋 pom.xml                          # Maven project configuration
├── 📄 README.md                         # Project documentation
└── ⚙️ sonar-project.properties           # SonarCloud configuration
```

## 🚀 Quick Start

### For Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/zetta1973/ecommerce-api.git
   cd ecommerce-api
   ```

2. **Set up environment**
   ```bash
   # Copy and configure environment variables
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Build and run**
   ```bash
   # Build
   ./scripts/build.sh local
   
   # Run tests
   ./scripts/test.sh unit
   
   # Start application
   java -jar target/ecommerce-api-1.1.8.jar
   ```

## 🔧 Development Tools

### API Testing (Postman)
- Import `postman/ecommerce-api-collection.json`
- Configure variables:
  - `baseUrl`: http://localhost:8080
  - `apiVersion`: /api/v1

### Container Development
```bash
# Build Docker image
docker build -t ecommerce-api:local .

# Run with environment
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev ecommerce-api:local
```

### Kubernetes Development
```bash
# Deploy to local Kubernetes
./scripts/deploy.sh local 1.1.8

# Deploy to staging
./scripts/deploy.sh staging 1.1.8

# Deploy to production
./scripts/deploy.sh production 1.1.8
```

## 🔒 CI/CD Pipelines

### GitHub Actions (Recommended)
- **Trigger**: Push to main/develop/feature branches
- **Stages**: Test → Security Analysis → Build → Deploy
- **Registry**: GitHub Container Registry (ghcr.io)

### Jenkins (Alternative)
- **Pipeline**: Jenkinsfile with multi-stage deployment
- **Security**: OWASP Dependency Check + SonarCloud analysis
- **Deployment**: Automatic on main branch

### Security Features
- **OWASP Dependency Check**: Scans for known vulnerabilities
- **SonarCloud**: Code quality analysis
- **Container Security**: Multi-stage Docker builds
- **Secrets Management**: Kubernetes secrets + GitHub Actions

## 📊 Monitoring & Observability

### Application Health
- **Health Endpoint**: `/actuator/health`
- **Metrics Endpoint**: `/actuator/metrics`
- **Info Endpoint**: `/actuator/info`

### Kubernetes Monitoring
- **Horizontal Pod Autoscaler**: Automatic scaling
- **Resource Limits**: Memory and CPU constraints
- **Health Probes**: Liveness and readiness checks

## 🛠️ Configuration

### Application Properties
- **Development**: `application-dev.properties`
- **Staging**: `application-staging.properties`
- **Production**: `application-prod.properties`

### Environment Variables
- **Database**: PostgreSQL connection settings
- **Security**: JWT secret and CORS settings
- **Kafka**: Bootstrap servers and topic configuration
- **Docker**: Registry and image tag settings

## 🔗 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/refresh` - Refresh JWT token

### Products
- `GET /api/v1/products` - List all products
- `POST /api/v1/products` - Create new product
- `GET /api/v1/products/{id}` - Get product details
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product

### Orders
- `GET /api/v1/orders` - List user orders
- `POST /api/v1/orders` - Create new order
- `GET /api/v1/orders/{id}` - Get order details

### Admin
- `GET /api/v1/admin/users` - List all users
- `GET /api/v1/admin/metrics` - Application metrics

## 📚 Documentation

- **API Documentation**: OpenAPI 3.0 specification
- **Code Coverage**: JaCoCo reports in SonarCloud
- **Dependencies**: OWASP vulnerability reports
- **Architecture**: Component and deployment diagrams

## 🧪 Testing Strategy

### Unit Tests
- **Framework**: JUnit 5
- **Coverage**: Target 80%+ coverage
- **Tools**: Mockito, Spring Boot Test

### Integration Tests
- **Database**: H2 in-memory for testing
- **API**: MockMvc for endpoint testing
- **Security**: Spring Security Test support

### Security Testing
- **OWASP Dependency Check**: Automated vulnerability scanning
- **Penetration Testing**: Manual testing with Postman collection
- **Code Analysis**: SonarCloud quality gates

## 🌍 Deployment Environments

### Development
- **Purpose**: Local development and testing
- **Database**: Local PostgreSQL or H2
- **Services**: All services running locally

### Staging
- **Purpose**: Pre-production testing
- **Database**: Staging PostgreSQL instance
- **Services**: Production-like setup

### Production
- **Purpose**: Live user-facing application
- **Database**: Production PostgreSQL cluster
- **Services**: Load-balanced, auto-scaling

## 🏗️ Version Management

- **Semantic Versioning**: MAJOR.MINOR.PATCH
- **Current Version**: 1.1.8
- **Release Process**: Automated through CI/CD
- **Tagging**: Git tags for releases

## 🤝 Contributing Guidelines

### Code Standards
- **Code Style**: Follow existing conventions
- **Testing**: Write unit tests for new features
- **Documentation**: Update README and API docs
- **Security**: Follow security best practices

### Development Workflow
1. Create feature branch from develop
2. Make changes and test locally
3. Commit with conventional commit messages
4. Create pull request to develop
5. Code review and automated tests
6. Merge to develop
7. Deploy to staging for testing
8. Merge develop to main for production

---

🎯 **This repository provides everything needed for full-stack development, testing, and deployment of the Ecommerce API!**