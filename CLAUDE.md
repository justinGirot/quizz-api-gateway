# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **API Gateway** for the Quiz microservices application. It serves as the single entry point for all client requests and routes them to the appropriate backend services (Auth, Question, Quiz services). Built with Spring Cloud Gateway MVC on Spring Boot 3.4.0 and Java 21.

## Architecture Context

This gateway is part of a larger microservices ecosystem:
- **Frontend** (React + Vite, port 5174) → **API Gateway** (port 8080) → Backend Services
- **Backend Services**:
  - Auth Service (port 8081): User authentication, JWT tokens, user management
  - Question Service (port 8082): Question CRUD, categories, difficulty levels
  - Quiz Service (port 8083): Quiz sessions, scoring, leaderboards

Each service has its own H2 database and communicates via REST APIs. The gateway handles routing, CORS, rate limiting, and request/response logging.

### Planned Routing Configuration

The gateway should route requests as follows:
```
/api/auth/**       → http://localhost:8081
/api/questions/**  → http://localhost:8082
/api/quizzes/**    → http://localhost:8083
```

See ARCHITECTURE.md for complete system design, data models, and communication patterns.

## Common Commands

### Build and Run
```bash
# Clean and build
./mvnw clean install

# Run application (port 8080)
./mvnw spring-boot:run

# Build without tests
./mvnw clean install -DskipTests

# Package as JAR
./mvnw package
```

### Testing
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ClassName

# Run with coverage
./mvnw test jacoco:report
```

### Development
```bash
# Check for dependency updates
./mvnw versions:display-dependency-updates

# Format code (if formatter plugin added)
./mvnw spring-javaformat:apply
```

## Implementation Status

**Current**: The project has only `pom.xml` configured. The `src/` directory structure and application code do not yet exist.

**Next Steps**: Create the Spring Boot application with:
1. Main application class with `@SpringBootApplication`
2. Configuration for Spring Cloud Gateway MVC routes
3. CORS configuration for frontend (origin: http://localhost:5174)
4. Application properties/YAML with port 8080 and route definitions
5. Actuator endpoints for health monitoring
6. Request/response logging filters
7. Error handling and fallback mechanisms

## Key Technologies

- **Java 21**: Use modern Java features (records, pattern matching, virtual threads if needed)
- **Spring Boot 3.4.0**: Spring Boot parent for dependency management
- **Spring Cloud Gateway MVC**: Synchronous gateway (not reactive WebFlux)
- **Spring Cloud 2024.0.2**: Cloud components version
- **Spring Boot Actuator**: Health checks and monitoring
- **Lombok**: Reduce boilerplate (enabled in maven-compiler-plugin)
- **Maven**: Build tool with wrapper included

## Gateway Implementation Guidelines

### Route Configuration
When implementing routes, use `application.yml` or Java configuration to define:
- Route predicates (path patterns)
- Filters (modify requests/responses, add headers)
- URI forwarding to backend services
- Load balancing (when multiple instances exist)

### CORS Configuration
Must allow requests from frontend origin (http://localhost:5174 for dev):
- Allow credentials
- Support preflight requests
- Configure allowed methods, headers

### Error Handling
Implement custom error responses for:
- Service unavailable (503) when backend down
- Gateway timeout (504)
- Not found (404) for unknown routes
- Unauthorized (401) for missing/invalid JWT tokens

### Logging
Add filters to log:
- Incoming request details (method, path, headers)
- Route resolution
- Response status and timing
- Correlation IDs for request tracing

### Security Considerations
- Validate JWT tokens (extract from Authorization header)
- Add security headers (HSTS, X-Content-Type-Options, etc.)
- Rate limiting per IP or user
- Request size limits
- Timeout configuration

## Testing Strategy

When writing tests:
1. **Unit Tests**: Test route configuration, filters, CORS setup
2. **Integration Tests**: Use `@SpringBootTest` with `@AutoConfigureMockMvc` to test routes
3. **Contract Tests**: Verify API contracts with backend services
4. **Mock Backend Services**: Use WireMock or MockWebServer for testing

## Related Repositories

When making changes, consider impact on:
- **quizz-auth-service**: Authentication endpoints at /api/auth/**
- **quizz-question-service**: Question management at /api/questions/**
- **quizz-quiz-service**: Quiz operations at /api/quizzes/**
- **Quizz_frontend**: Frontend making requests through this gateway

## Configuration Notes

- **Port**: 8080 (must not conflict with backend services on 8081-8083)
- **Profiles**: Use Spring profiles (dev, prod) for environment-specific config
- **Actuator**: Enable health endpoint at `/actuator/health` for monitoring
- **Service URLs**: Should be configurable via environment variables or properties
