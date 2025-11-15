# Quiz API Gateway

Production-ready API Gateway for the Quiz microservices application. Routes requests from the frontend to backend services with service discovery, circuit breaker protection, and comprehensive security features.

## Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (included via Maven Wrapper)
- **Eureka Server** running on port 8761 (service discovery)
- Backend services registered with Eureka:
  - Auth Service (service name: `auth-service`)
  - Question Service (service name: `question-service`, optional)
  - Quiz Service (service name: `quiz-service`, optional)

### Installing Java 21

#### Windows
Download and install from [Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or use [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html)

Set JAVA_HOME:
```powershell
setx JAVA_HOME "C:\Program Files\Java\jdk-21"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

#### Linux/Mac
```bash
# Using SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
sdk install java 21-open

# Or download from https://jdk.java.net/21/
```

Verify installation:
```bash
java -version
# Should show: java version "21.x.x"
```

## Architecture

This gateway serves as the single entry point with service discovery and fault tolerance:

```
Frontend (React, port 5174)
        ↓
  API Gateway (port 8080)
  [Circuit Breaker, Security Headers, Request Limits]
        ↓
   Eureka Server (port 8761)
   [Service Discovery & Registration]
        ↓
   ┌────┴────┬────────┬─────────┐
   ↓         ↓        ↓         ↓
Auth      Question  Quiz    Future
Service   Service   Service Services
```

### Key Features

- ✅ **Service Discovery**: Netflix Eureka for dynamic service registration
- ✅ **Circuit Breaker**: Resilience4j with automatic fallback responses
- ✅ **Security Headers**: OWASP recommended headers (XSS, Clickjacking, MIME sniffing protection)
- ✅ **Request Size Limits**: Prevents memory exhaustion (10MB max)
- ✅ **CORS**: Flexible origin patterns for development and production
- ✅ **Distributed Tracing**: Correlation IDs for request tracking
- ✅ **Memory-Safe Logging**: Conditional content caching based on size
- ✅ **Retry Mechanism**: Exponential backoff for transient failures

## Quick Start

### 1. Build the Application
```bash
# Clean and build
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

The gateway will start on **port 8080**.

### 3. Verify It's Running
```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## API Routes

The gateway routes requests to backend services using service discovery:

| Route Pattern      | Target Service    | Discovery Name      | Description                    |
|-------------------|-------------------|---------------------|--------------------------------|
| `/api/auth/**`    | Auth Service      | `lb://auth-service` | Authentication & user mgmt     |
| `/api/questions/**` | Question Service | `lb://question-service` | Question CRUD operations   |
| `/api/quizzes/**` | Quiz Service      | `lb://quiz-service` | Quiz sessions & scoring       |

Each route is protected by a circuit breaker with automatic fallback endpoints.

## Configuration

### Environment Variables

You can override default values using environment variables:

```bash
# Server configuration
export SERVER_PORT=8080

# Eureka Server URL (service discovery)
export EUREKA_SERVER_URL=http://localhost:8761/eureka/

# CORS configuration (configured in application.yml)
# Uses allowedOriginPatterns: http://localhost:[*] for development
```

### Configuration Properties

All features are configurable via `application.yml`:

- **CORS Settings** (`cors.*`): Origin patterns, allowed headers, credentials
- **Security Headers** (`security.headers.*`): XSS, clickjacking, HSTS, CSP
- **Request Limits** (`gateway.request-limits.*`): Size limits, caching behavior
- **Circuit Breaker** (`resilience4j.circuitbreaker.*`): Thresholds, timeouts, retry
- **Eureka Client** (`eureka.client.*`): Service discovery configuration

### Application Profiles

```bash
# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or set environment variable
export SPRING_PROFILES_ACTIVE=prod
```

## CORS Configuration

The gateway is configured to accept requests from:
- `http://localhost:5174` (development frontend)
- `http://localhost:3000` (production frontend)

Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH

## Logging

Logs are written to:
- Console (for development)
- `logs/api-gateway.log` (for production)

Log levels can be configured in `application.yml`.

### Request Correlation

Each request is assigned a correlation ID for tracing:
- Header: `X-Correlation-ID`
- Automatically generated if not provided
- Included in all log messages

## Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=CorsConfigTest
```

### Integration Testing

Test the gateway with backend services:

```bash
# 1. Start auth-service on port 8081
cd ../quizz-auth-service && ./mvnw spring-boot:run &

# 2. Start gateway
./mvnw spring-boot:run

# 3. Test authentication flow
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

## Monitoring

### Actuator Endpoints

Available at `/actuator/*`:

- `/actuator/health` - Health status with circuit breaker details
- `/actuator/info` - Application information
- `/actuator/circuitbreakers` - Circuit breaker status for all services
- `/actuator/circuitbreakerevents` - Recent circuit breaker events

Example:
```bash
# Check overall health
curl http://localhost:8080/actuator/health

# Check circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# View circuit breaker events (state changes, failures)
curl http://localhost:8080/actuator/circuitbreakerevents
```

### Circuit Breaker States

Monitor circuit breaker behavior:
- **CLOSED**: Normal operation, requests flowing through
- **OPEN**: Too many failures, using fallback responses
- **HALF_OPEN**: Testing if service recovered

Thresholds (configurable in `application.yml`):
- Failure rate: 50%
- Slow call rate: 50% (calls > 2 seconds)
- Wait duration: 30 seconds before retry

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/quizz/apigateway/
│   │   ├── ApiGatewayApplication.java    # Main application (@EnableDiscoveryClient)
│   │   ├── config/
│   │   │   ├── GatewayConfig.java        # Route configuration with circuit breakers
│   │   │   ├── CorsConfig.java           # CORS configuration
│   │   │   ├── CorsProperties.java       # CORS properties (@ConfigurationProperties)
│   │   │   ├── CircuitBreakerConfig.java # Resilience4j configuration
│   │   │   ├── SecurityHeadersProperties.java  # Security headers properties
│   │   │   └── RequestLimitProperties.java     # Request size limit properties
│   │   ├── filter/
│   │   │   ├── LoggingFilter.java        # Request/response logging with correlation IDs
│   │   │   ├── SecurityHeadersFilter.java # Security headers filter
│   │   │   └── RequestSizeLimitFilter.java # Request size validation
│   │   └── controller/
│   │       └── FallbackController.java   # Circuit breaker fallback endpoints
│   └── resources/
│       └── application.yml               # Comprehensive configuration
└── test/
    ├── java/com/quizz/apigateway/
    │   ├── ApiGatewayApplicationTests.java
    │   └── config/
    │       └── CorsConfigTest.java
    └── resources/
        └── application-test.yml
```

### Adding New Routes

Add a new route bean in `GatewayConfig.java`:

```java
@Bean
public RouterFunction<ServerResponse> newServiceRoute() {
    return route("new_service")
            .route(path("/api/newservice/**"), http("lb://new-service"))
            .filter(circuitBreaker("newServiceCircuitBreaker", "/fallback/newservice"))
            .build();
}
```

Then add circuit breaker configuration in `application.yml`:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      newServiceCircuitBreaker:
        base-config: default
        register-health-indicator: true
```

And create a fallback endpoint in `FallbackController.java`:

```java
@GetMapping("/newservice")
@PostMapping("/newservice")
public ResponseEntity<Map<String, Object>> newServiceFallback() {
    return createFallbackResponse("New Service", "The service is temporarily unavailable...");
}
```

## Troubleshooting

### Eureka Server not reachable

**Error**: `TransportException: Cannot execute request on any known server` or `Connection refused: localhost:8761`

**Solution**: Ensure Eureka Server is running:
```bash
# Check Eureka Server dashboard
curl http://localhost:8761

# Or open in browser
# http://localhost:8761
```

If Eureka is on a different host, set the environment variable:
```bash
export EUREKA_SERVER_URL=http://eureka-host:8761/eureka/
```

### Services not registered with Eureka

**Error**: Circuit breaker immediately open, `No instances available for service`

**Solution**: Verify services are registered with Eureka:
1. Open Eureka dashboard: `http://localhost:8761`
2. Check "Instances currently registered with Eureka" section
3. Ensure services appear with correct names: `AUTH-SERVICE`, `QUESTION-SERVICE`, `QUIZ-SERVICE`

If services aren't registered:
- Check service `application.yml` has `eureka.client.register-with-eureka: true`
- Verify service name matches route configuration
- Check service logs for Eureka connection errors

### Circuit breaker stuck in OPEN state

**Error**: Consistent fallback responses even when service is healthy

**Solution**: Circuit breaker needs time to recover:
1. Wait 30 seconds (default wait-duration-in-open-state)
2. Circuit breaker will transition to HALF_OPEN
3. If service responds successfully, circuit closes

To manually reset, restart the gateway:
```bash
./mvnw spring-boot:run
```

Check circuit breaker status:
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

### Request too large (413 Payload Too Large)

**Error**: `HTTP 413 Payload Too Large` or `Request size exceeds maximum allowed`

**Solution**: Request exceeds the 10MB limit. Options:
1. Reduce request payload size (recommended)
2. Increase limit in `application.yml` (use with caution):
   ```yaml
   gateway:
     request-limits:
       max-request-size: 20971520  # 20MB in bytes
   ```

**Note**: Large request limits can cause memory exhaustion. Only increase if necessary.

### CORS errors in browser

**Error**: `Access to XMLHttpRequest has been blocked by CORS policy`

**Solutions**:
1. Verify frontend URL matches origin patterns in `application.yml`:
   ```yaml
   cors:
     allowed-origin-patterns:
       - "http://localhost:[*]"
   ```
2. Check that credentials are being sent correctly
3. Ensure preflight (OPTIONS) requests are not failing
4. Verify `Access-Control-Allow-Credentials: true` in response headers

### Port already in use

**Error**: `Port 8080 is already in use`

**Solution**: Change port via environment variable:
```bash
export SERVER_PORT=8081
./mvnw spring-boot:run
```

Or kill the process using port 8080:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

## Related Repositories

- [Quizz Frontend](https://github.com/justinGirot/Quizz_frontend) - React frontend
- [Auth Service](https://github.com/justinGirot/quizz-auth-service) - Authentication service
- [Question Service](https://github.com/justinGirot/quizz-question-service) - Question management
- [Quiz Service](https://github.com/justinGirot/quizz-quiz-service) - Quiz sessions & scoring

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests: `./mvnw test`
4. Create a pull request

## License

See LICENSE file for details.
