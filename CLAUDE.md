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

### Routing Configuration

The gateway uses Spring Cloud Netflix Eureka for service discovery and routes requests as follows:
```
/api/auth/**       → lb://auth-service      (service discovery)
/api/questions/**  → lb://question-service  (service discovery)
/api/quizzes/**    → lb://quiz-service      (service discovery)
```

Each route is protected by Resilience4j circuit breaker with automatic fallback endpoints.

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

**Current Status**: ✅ Fully implemented and ready for deployment

**Completed Features**:
1. ✅ Main application class with `@SpringBootApplication` and `@EnableDiscoveryClient`
2. ✅ Spring Cloud Gateway MVC routes with service discovery (Eureka)
3. ✅ CORS configuration with flexible origin patterns and explicit headers
4. ✅ Application.yml with comprehensive configuration for all features
5. ✅ Actuator endpoints for health monitoring and circuit breaker metrics
6. ✅ Request/response logging filters with correlation IDs and memory safeguards
7. ✅ Circuit breaker pattern with Resilience4j and automatic fallback endpoints
8. ✅ Security headers (X-Content-Type-Options, X-Frame-Options, XSS-Protection, HSTS, CSP)
9. ✅ Request size limits to prevent memory exhaustion and DoS attacks
10. ✅ Retry mechanism with exponential backoff
11. ✅ Time limiter for request timeout handling

**Architecture Highlights**:
- **Service Discovery**: Netflix Eureka client for dynamic service registration
- **Fault Tolerance**: Circuit breaker pattern with configurable thresholds (50% failure rate)
- **Security**: Comprehensive security headers and request validation
- **Observability**: Correlation IDs, structured logging, actuator endpoints
- **Resilience**: Automatic retries, circuit breaking, fallback responses
- **Memory Safety**: Conditional content caching based on size and content type

## Key Technologies

- **Java 21**: Use modern Java features (records, pattern matching, virtual threads if needed)
- **Spring Boot 3.4.0**: Spring Boot parent for dependency management
- **Spring Cloud Gateway MVC**: Synchronous gateway (not reactive WebFlux)
- **Spring Cloud 2024.0.2**: Cloud components version
- **Spring Cloud Netflix Eureka Client**: Service discovery and registration
- **Resilience4j**: Circuit breaker, retry, and time limiter patterns
- **Spring Boot Actuator**: Health checks, metrics, and circuit breaker monitoring
- **Lombok**: Reduce boilerplate (enabled in maven-compiler-plugin)
- **Maven**: Build tool with wrapper included

## Gateway Implementation Guidelines

### Route Configuration
Routes are configured in `GatewayConfig.java` using RouterFunction beans with:
- **Path predicates**: Pattern matching for routing (e.g., `/api/auth/**`)
- **Service discovery URIs**: Load-balanced URIs using `lb://` prefix (e.g., `lb://auth-service`)
- **Circuit breaker filters**: Resilience4j circuit breakers with fallback paths
- **Client-side load balancing**: Automatic distribution across service instances

Example:
```java
@Bean
public RouterFunction<ServerResponse> authServiceRoute() {
    return route("auth_service")
            .route(path("/api/auth/**"), http("lb://auth-service"))
            .filter(circuitBreaker("authServiceCircuitBreaker", "/fallback/auth"))
            .build();
}
```

### CORS Configuration
Implemented in `CorsConfig.java` with `CorsProperties` for externalized configuration:
- **Origin patterns**: `http://localhost:[*]` for flexible dev port matching
- **Explicit headers**: Authorization, Content-Type, Accept, X-Correlation-ID, etc. (no wildcards)
- **Exposed headers**: X-Correlation-ID, Content-Disposition
- **Credentials**: Enabled for cookie and auth header support
- **Preflight caching**: 1-hour cache for OPTIONS requests
- **Configurable via application.yml**: Separate dev and prod origin settings

### Error Handling & Fallback
Implemented with circuit breaker pattern in `FallbackController.java`:
- **Service unavailable (503)**: Circuit breaker fallback responses with helpful error messages
- **Automatic fallback**: When failure/slow call threshold exceeds 50% (configurable)
- **Circuit states**: Closed → Open (30s) → Half-open → Closed
- **Retry mechanism**: 3 attempts with exponential backoff (500ms base, 2x multiplier)
- **Request size limits**: HTTP 413 for requests exceeding 10MB
- **Fallback endpoints**: `/fallback/auth`, `/fallback/questions`, `/fallback/quizzes`

### Logging
Implemented in `LoggingFilter.java` with memory-safe practices:
- **Correlation IDs**: Auto-generated UUID for each request (X-Correlation-ID header)
- **Request metadata**: Method, URI, remote address, content type, size
- **Response metadata**: Status code, duration in milliseconds
- **Memory safeguards**:
  - Only caches content < 1MB (configurable)
  - Skips binary content types (images, videos, multipart)
  - Logs metadata for all requests, body only for small requests (if enabled)
- **Masked credentials**: Authorization headers are partially masked in logs
- **Actuator exclusion**: Skips logging for `/actuator/**` endpoints

### Security Considerations
Implemented in `SecurityHeadersFilter.java` and `RequestSizeLimitFilter.java`:
- **Security headers** (configurable, enabled by default):
  - `X-Content-Type-Options: nosniff` - Prevents MIME type sniffing
  - `X-Frame-Options: DENY` - Prevents clickjacking attacks
  - `X-XSS-Protection: 1; mode=block` - Enables XSS filter
  - `Strict-Transport-Security` - Forces HTTPS (disabled in dev, enable in prod)
  - `Content-Security-Policy` - Advanced security policy (optional, disabled by default)
  - `Referrer-Policy: strict-origin-when-cross-origin` - Controls referrer info
  - `Permissions-Policy` - Restricts browser features
- **Request size limits**:
  - Hard limit: 10MB (returns HTTP 413 Payload Too Large)
  - Loggable size: 1MB (larger requests only log metadata)
  - Cacheable size: 1MB (larger requests not cached in memory)
- **Timeout configuration**: 3-second timeout with time limiter
- **JWT validation**: To be implemented by auth service (gateway forwards tokens)

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
- **Eureka Server**: Defaults to `http://localhost:8761/eureka/` (configurable via `EUREKA_SERVER_URL`)
- **Profiles**: Use Spring profiles (dev, prod) for environment-specific config
- **Actuator Endpoints**:
  - `/actuator/health` - Health status with circuit breaker details
  - `/actuator/circuitbreakers` - Circuit breaker status
  - `/actuator/circuitbreakerevents` - Circuit breaker events
- **Service Discovery**: Services register with Eureka using their spring.application.name
- **Configuration Files**:
  - `application.yml` - Main configuration
  - `CorsProperties` - CORS settings (prefix: `cors`)
  - `SecurityHeadersProperties` - Security headers (prefix: `security.headers`)
  - `RequestLimitProperties` - Size limits (prefix: `gateway.request-limits`)
