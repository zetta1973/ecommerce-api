# AGENTS.md

This file contains guidelines and commands for agentic coding agents working in this Java Spring Boot e-commerce API repository.

## Build & Test Commands

### Core Commands
- **Build project**: `./mvnw clean compile` or `mvn clean compile`
- **Run tests**: `./mvnw test` or `mvn test`
- **Run single test**: `./mvnw test -Dtest=ClassName` (e.g., `./mvnw test -Dtest=AuthControllerTest`)
- **Run single test method**: `./mvnw test -Dtest=ClassName#methodName` (e.g., `./mvnw test -Dtest=AuthControllerTest#shouldRegisterUser`)
- **Package application**: `./mvnw clean package`
- **Generate test coverage report**: `./mvnw jacoco:report`
- **Check test coverage**: `./mvnw jacoco:check`
- **Run application locally**: `./mvnw spring-boot:run`

### Docker Commands
- **Build Docker image**: `docker build -t ecommerce-api:1.0.x .`
- **Run with Docker Compose**: `docker-compose up --build`

## Git Commit & Push Guidelines

### IMPORTANT: Always Ask for Permission
**CRITICAL RULE**: Before executing ANY git commit or git push commands, you MUST ask the user for explicit permission:
- "I've completed [task]. Would you like me to commit these changes?"
- "Would you like me to push the commit to remote?"
- Only proceed after receiving confirmation from the user

### Commit Message Convention (Conventional Commits)

Follow the Conventional Commits specification:
```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Commit Types
- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring (neither bug fix nor feature)
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Build system or dependency changes
- **ci**: CI/CD configuration changes
- **chore**: Maintenance tasks

#### Commit Scopes
Common scopes for this project:
- **k8s**: Kubernetes manifests
- **ci**: GitHub Actions, CI/CD
- **controller**: REST controllers
- **service**: Service layer
- **repository**: JPA repositories
- **model**: JPA entities
- **security**: Authentication/authorization
- **kafka**: Kafka components
- **config**: Configuration files

#### Commit Message Guidelines
- **Subject**: 50 characters max, imperative mood, no period at end
- **Body**: Explain WHAT and WHY, not HOW
- **Footer**: Breaking changes, issue references

#### Examples
```
feat(auth): add JWT refresh token support

Add refresh token endpoint to allow clients to obtain new access tokens
without re-authenticating. Includes validation and security checks.

Closes #123
```

```
fix(k8s): resolve Kind image pull issues and namespace inconsistency

- Change imagePullPolicy to IfNotPresent to use locally loaded images in Kind
- Align namespace to default across all Kubernetes manifests for consistency
- Remove redundant postgres-service.yaml reference

This fixes deployment failures in CI pipeline where Kind cluster
was trying to pull non-existent images from remote registry.
```

```
ci: add pod readiness checks and enhanced debugging for Kind deployment

- Add dedicated step to wait for pod readiness before testing
- Show application logs before endpoint testing for better debugging
- Reduce initial sleep time as readiness is now verified
- Include full logs on test failure for troubleshooting
```

```
refactor(service): extract user validation logic into separate class

Extract validation logic from UserService into UserValidator to improve
code organization and enable reuse across controllers.
```

### Git Commands Workflow

#### 1. Check Status (Before Commit)
```bash
git status
```

#### 2. Review Changes
```bash
git diff              # Show unstaged changes
git diff --staged      # Show staged changes
```

#### 3. Stage Changes
```bash
git add <file>              # Stage specific file
git add .                    # Stage all changes
git add *.yaml               # Stage all YAML files
git add k8s/                 # Stage all files in k8s directory
```

#### 4. Create Commit (ASK PERMISSION FIRST!)
```bash
git commit -m "type(scope): subject

body"
```

#### 5. Verify Commit
```bash
git log -1           # Show last commit
git status           # Verify working tree is clean
```

#### 6. Push to Remote (ASK PERMISSION FIRST!)
```bash
git push                      # Push current branch to remote
git push origin main          # Push main branch
git push -u origin <branch>   # Push and set upstream
```

### Commit Process Checklist

Before creating a commit, verify:
1. ✅ Run tests: `./mvnw test`
2. ✅ Build succeeds: `./mvnw clean package`
3. ✅ Review changes with `git diff`
4. ✅ Ensure all changed files are relevant
5. ✅ Write clear commit message following conventions
6. ⚠️ **ASK USER FOR PERMISSION to commit**

Before pushing to remote:
1. ✅ Commit message is correct and follows conventions
2. ✅ No sensitive information in changes
3. ✅ Tests pass locally
4. ✅ Review commit with `git log -1`
5. ⚠️ **ASK USER FOR PERMISSION to push**

## Code Style Guidelines

### Project Structure
```
src/main/java/com/ecommerce/
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # JPA repositories
├── model/          # JPA entities
├── dto/            # Data Transfer Objects
├── security/       # Security components
├── config/         # Spring configuration
└── kafka/          # Kafka components
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `AuthController`, `UserService`)
- **Methods**: camelCase (e.g., `registerUser`, `findById`)
- **Variables**: camelCase (e.g., `userRepository`, `passwordEncoder`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `JWT_EXPIRATION`)
- **Packages**: lowercase (e.g., `com.ecommerce.controller`)
- **Files**: PascalCase matching class name

### Import Organization
1. Java standard library imports (`java.*`)
2. Jakarta EE imports (`jakarta.*`)
3. Spring framework imports (`org.springframework.*`)
4. Third-party library imports
5. Project-specific imports (`com.ecommerce.*`)

Use `*` imports sparingly, prefer explicit imports.

### Code Formatting
- **Indentation**: 4 spaces, no tabs
- **Line length**: Maximum 120 characters
- **Braces**: Opening brace on same line, closing brace on new line
- **Spacing**: One space around operators, after commas, after keywords
- **Blank lines**: Between methods, between logical sections

### Annotations
- **Lombok**: Use `@Data` for entities with getters/setters, `@RequiredArgsConstructor` for dependency injection
- **Spring**: `@RestController` for controllers, `@Service` for services, `@Repository` for repositories
- **Validation**: Use `@Valid` for request bodies, `@NotBlank`, `@Email` etc. on DTO fields
- **JPA**: `@Entity`, `@Table`, `@Id`, `@GeneratedValue` for entities

### Error Handling
- Use `@RestControllerAdvice` with `@ExceptionHandler` for global exception handling
- Return `ResponseEntity` with appropriate HTTP status codes
- Use `Map.of("error", "message")` for error responses
- Validate input using Jakarta validation annotations

### Security Patterns
- JWT-based authentication with `JwtUtil` class
- Role-based access control using `@PreAuthorize`
- Password encoding with BCrypt
- Method-level security enabled

### Database & JPA
- Entities implement `UserDetails` when they represent users
- Use `@Table(name = "table_name")` for custom table names
- Relationships: `@ManyToOne`, `@OneToMany`, etc.
- Repository interfaces extend `JpaRepository`

### Testing Guidelines
- Use JUnit 5 (`@Test`, `@BeforeEach`, `@DisplayName`)
- Mock dependencies with Mockito
- Use `MockMvc` for controller tests
- Test file naming: `ClassNameTest.java`
- Arrange-Act-Assert pattern in tests
- Use `@SpringBootTest` for integration tests
- H2 in-memory database for testing

### DTO Patterns
- Use Lombok `@Data` annotation
- Include validation annotations (`@NotBlank`, `@Email`, etc.)
- Separate request/response DTOs when needed
- Use meaningful names like `UserRegisterDto`, `ProductResponseDto`

### Logging
- Use SLF4J with `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`
- Log important events, errors, and debugging information
- Use appropriate log levels (INFO, WARN, ERROR)

### Kafka Integration
- Producers in `KafkaProducer` class
- Consumers in `KafkaConsumer` class
- Event classes in dedicated package
- Handle serialization/deserialization properly

### API Documentation
- OpenAPI 3.0 specification in `src/main/resources/openapi.yaml`
- Use meaningful endpoint paths
- Include request/response examples
- Document authentication requirements

### Configuration
- Main config: `src/main/resources/application.yml`
- Test config: `src/test/resources/application-test.yml`
- Use Spring profiles for different environments
- Environment-specific properties

## Development Workflow

1. **Before making changes**:
   - Run existing tests: `./mvnw test`
   - Understand existing patterns in similar files

2. **When adding new features**:
   - Follow the established package structure
   - Create corresponding test files
   - Add proper validation and error handling
   - Update OpenAPI spec if needed

3. **After making changes**:
   - Run tests: `./mvnw test`
   - Check test coverage: `./mvnw jacoco:report`
   - Build project: `./mvnw clean package`
   - Run linting if configured

## Quality Standards
- Test coverage requirement: 100% (enforced by JaCoCo)
- All public methods should be tested
- Include positive and negative test cases
- Validate input parameters and handle errors gracefully
- Follow REST API best practices

## Common Patterns

### Controller Pattern
```java
@RestController
@RequestMapping("/path")
public class ExampleController {
    private final ExampleService service;
    
    public ExampleController(ExampleService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ExampleDto dto) {
        // implementation
    }
}
```

### Service Pattern
```java
@Service
public class ExampleService {
    private final ExampleRepository repository;
    
    public ExampleService(ExampleRepository repository) {
        this.repository = repository;
    }
    
    public Example create(ExampleDto dto) {
        // implementation
    }
}
```

### Entity Pattern
```java
@Data
@Entity
@Table(name = "examples")
public class Example {
    @Id @GeneratedValue
    private Long id;
    
    // fields
}
```
