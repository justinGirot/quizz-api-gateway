# Quiz API Gateway

API Gateway for the Quiz microservices application. Routes requests from the frontend to backend services (Auth, Question, Quiz).

## Prerequisites

- **Java 21** or higher
- **Maven 3.9+** (included via Maven Wrapper)
- Backend services running:
  - Auth Service on port 8081
  - Question Service on port 8082 (optional)
  - Quiz Service on port 8083 (optional)

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

This gateway serves as the single entry point for all client requests:

```
Frontend (React, port 5174)
        ↓
  API Gateway (port 8080)
        ↓
   ┌────┴────┬────────┬─────────┐
   ↓         ↓        ↓         ↓
Auth-8081  Question  Quiz    Future
           -8082     -8083   Services
```

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

The gateway routes requests to backend services:

| Route Pattern      | Target Service    | Port | Description                    |
|-------------------|-------------------|------|--------------------------------|
| `/api/auth/**`    | auth-service      | 8081 | Authentication & user mgmt     |
| `/api/questions/**` | question-service | 8082 | Question CRUD operations      |
| `/api/quizzes/**` | quiz-service      | 8083 | Quiz sessions & scoring       |

## Configuration

### Environment Variables

You can override default values using environment variables:

```bash
# Server configuration
export SERVER_PORT=8080

# Service URLs
export AUTH_SERVICE_URL=http://localhost:8081
export QUESTION_SERVICE_URL=http://localhost:8082
export QUIZ_SERVICE_URL=http://localhost:8083

# Frontend URLs (for CORS)
export FRONTEND_URL=http://localhost:5174
export FRONTEND_PROD_URL=http://localhost:3000
```

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

- `/actuator/health` - Health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

Example:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/quizz/apigateway/
│   │   ├── ApiGatewayApplication.java    # Main application
│   │   ├── config/
│   │   │   └── CorsConfig.java           # CORS configuration
│   │   ├── filter/
│   │   │   └── LoggingFilter.java        # Request/response logging
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java # Error handling
│   └── resources/
│       └── application.yml               # Application configuration
└── test/
    ├── java/com/quizz/apigateway/
    │   ├── ApiGatewayApplicationTests.java
    │   └── config/
    │       └── CorsConfigTest.java
    └── resources/
        └── application-test.yml
```

### Adding New Routes

Edit `src/main/resources/application.yml`:

```yaml
spring:
  cloud:
    gateway:
      mvc:
        routes:
          - id: new-service
            uri: http://localhost:8084
            predicates:
              - Path=/api/newservice/**
            filters:
              - StripPrefix=0
```

## Troubleshooting

### Gateway can't connect to backend services

**Error**: `Service Unavailable` or `ResourceAccessException`

**Solution**: Ensure backend services are running:
```bash
# Check if services are listening
curl http://localhost:8081/actuator/health  # auth-service
curl http://localhost:8082/actuator/health  # question-service
curl http://localhost:8083/actuator/health  # quiz-service
```

### CORS errors in browser

**Error**: `Access to XMLHttpRequest has been blocked by CORS policy`

**Solutions**:
1. Verify frontend URL is in allowed origins (check `application.yml`)
2. Check that credentials are being sent correctly
3. Ensure preflight (OPTIONS) requests are not failing

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
