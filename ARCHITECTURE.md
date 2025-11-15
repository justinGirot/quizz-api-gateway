# Quiz Application - Microservices Architecture

This document describes the overall architecture of the Quiz application microservices system.

## Architecture Overview

The Quiz application follows a microservices architecture pattern with service discovery:

```
┌─────────────┐
│   Frontend  │ (React + Vite)
│  (Port 5174)│
└──────┬──────┘
       │
       │ HTTP/REST
       │
┌──────▼──────────────┐
│   API Gateway       │ (Spring Cloud Gateway)
│   (Port 8080)       │ ─────┐
└──────┬──────────────┘      │
       │                     │ Service Discovery
       │                     │
       ├─────────┬───────────┼─────────┬──────────┐
       │         │           │         │          │
┌──────▼──────┐  │   ┌───────▼────────┐│          │
│Auth Service │  │   │ Eureka Server  ││          │
│ (Port 8081) │──┘   │  (Port 8761)   ││          │
└─────────────┘      └────────────────┘│          │
       │                     ▲          │          │
┌──────▼──────┐              │  ┌───────▼──────┐  │  ┌────────────────┐
│   H2 DB     │      Register│  │Question Svc  │──┘  │  Quiz Svc      │
│  (File)     │              │  │ (Port 8082)  │     │  (Port 8083)   │
└─────────────┘              │  └──────┬───────┘     └────────┬───────┘
                             │         │                      │
                             └─────────┘              ┌───────▼──────┐
                                     ┌────────────────┤   H2 DB      │
                             ┌───────▼──────┐         │  (File)      │
                             │   H2 DB      │         └──────────────┘
                             │  (File)      │
                             └──────────────┘
```

**Key Components**:
- **Eureka Server** (8761): Service registry and discovery
- **API Gateway** (8080): Routes requests and discovers services via Eureka
- **Microservices** (8081-8083): Register with Eureka at startup
- **Frontend** (5174): Communicates only with API Gateway

## Services

### 1. Frontend (quizz_frontend)
- **Repository**: https://github.com/justinGirot/Quizz_frontend
- **Technology**: React 18.3, Vite 6.x
- **Port**: 5174 (development)
- **Responsibilities**:
  - User interface for quiz application
  - Authentication UI (login/signup)
  - Quiz taking interface
  - Question management interface
  - User dashboard and scoring

### 2. API Gateway (quizz-api-gateway)
- **Repository**: https://github.com/justinGirot/quizz-api-gateway
- **Technology**: Spring Boot 3.4, Spring Cloud Gateway, Java 21
- **Port**: 8080
- **Responsibilities**:
  - Single entry point for all client requests
  - Request routing to appropriate microservices
  - Load balancing across service instances
  - CORS configuration
  - Rate limiting and throttling
  - Request/response logging
  - API versioning

**Routing Configuration**:
```yaml
/api/auth/**        → auth-service (8081)
/api/questions/**   → question-service (8082)
/api/quizzes/**     → quiz-service (8083)
```

### 3. Eureka Server (quizz-eureka-server)
- **Repository**: https://github.com/justinGirot/quizz-eureka-server
- **Technology**: Spring Boot 3.4, Spring Cloud Netflix Eureka Server, Java 21
- **Port**: 8761
- **Responsibilities**:
  - Service registry and discovery
  - Service health monitoring
  - Dynamic service instance tracking
  - Load balancing support
  - Service metadata management

**Key Features**:
- Services register themselves at startup
- API Gateway discovers services dynamically
- Heartbeat-based health checking
- Web dashboard for service monitoring

**Configuration**:
```yaml
eureka:
  client:
    register-with-eureka: false  # Eureka doesn't register with itself
    fetch-registry: false
  server:
    enable-self-preservation: false  # Disabled in development
```

**Dashboard Access**: `http://localhost:8761`

**Registered Services**:
- api-gateway
- auth-service
- question-service
- quiz-service

### 4. Auth Service (quizz-auth-service)
- **Repository**: https://github.com/justinGirot/quizz-auth-service
- **Technology**: Spring Boot 3.4, Spring Security, Spring Data JPA, Java 21
- **Port**: 8081
- **Database**: H2 (file-based: `./data/auth.db`)
- **Responsibilities**:
  - User registration and authentication
  - JWT token generation and validation
  - User profile management
  - Password hashing and security
  - Session management

**Key Endpoints**:
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User authentication
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user profile
- `PUT /api/auth/profile` - Update user profile

**Data Model**:
```java
User {
  Long id;
  String email;
  String password; // BCrypt hashed
  String firstName;
  String lastName;
  LocalDateTime createdAt;
  LocalDateTime lastLogin;
  Set<Role> roles;
}
```

### 4. Question Service (quizz-question-service)
- **Repository**: https://github.com/justinGirot/quizz-question-service
- **Technology**: Spring Boot 3.4, Spring Data JPA, Java 21
- **Port**: 8082
- **Database**: H2 (file-based: `./data/questions.db`)
- **Responsibilities**:
  - Question creation, retrieval, update, deletion (CRUD)
  - Question categorization and tagging
  - Question difficulty levels
  - Question validation
  - Bulk question import/export

**Key Endpoints**:
- `POST /api/questions` - Create question
- `GET /api/questions/{id}` - Get question by ID
- `GET /api/questions` - List questions (with filters)
- `PUT /api/questions/{id}` - Update question
- `DELETE /api/questions/{id}` - Delete question
- `GET /api/questions/random` - Get random questions
- `GET /api/questions/category/{category}` - Get questions by category

**Data Model**:
```java
Question {
  Long id;
  String text;
  QuestionType type; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
  String category;
  DifficultyLevel difficulty; // EASY, MEDIUM, HARD
  List<Answer> answers;
  String correctAnswerId;
  Integer points;
  LocalDateTime createdAt;
  Long createdBy; // User ID
}

Answer {
  String id;
  String text;
  boolean isCorrect;
}
```

### 5. Quiz Service (quizz-quiz-service)
- **Repository**: https://github.com/justinGirot/quizz-quiz-service
- **Technology**: Spring Boot 3.4, Spring Data JPA, Java 21
- **Port**: 8083
- **Database**: H2 (file-based: `./data/quizzes.db`)
- **Responsibilities**:
  - Quiz creation and configuration
  - Quiz session management
  - Score calculation and tracking
  - Leaderboard management
  - Quiz participation tracking
  - Quiz results and analytics

**Key Endpoints**:
- `POST /api/quizzes` - Create quiz
- `GET /api/quizzes/{id}` - Get quiz details
- `GET /api/quizzes` - List available quizzes
- `POST /api/quizzes/{id}/start` - Start quiz session
- `POST /api/quizzes/sessions/{sessionId}/answer` - Submit answer
- `POST /api/quizzes/sessions/{sessionId}/complete` - Complete quiz
- `GET /api/quizzes/{id}/leaderboard` - Get quiz leaderboard
- `GET /api/quizzes/sessions/{sessionId}/results` - Get quiz results

**Data Model**:
```java
Quiz {
  Long id;
  String title;
  String description;
  List<Long> questionIds; // References to Question Service
  Integer timeLimit; // in minutes
  Integer maxAttempts;
  LocalDateTime startDate;
  LocalDateTime endDate;
  Long createdBy; // User ID
}

QuizSession {
  Long id;
  Long quizId;
  Long userId;
  SessionStatus status; // IN_PROGRESS, COMPLETED, ABANDONED
  LocalDateTime startedAt;
  LocalDateTime completedAt;
  Integer score;
  List<QuizAnswer> answers;
}

QuizAnswer {
  Long questionId;
  String answerId;
  boolean isCorrect;
  Integer pointsAwarded;
  LocalDateTime answeredAt;
}
```

## Communication Patterns

### 1. Synchronous Communication (REST)
- Frontend → API Gateway → Services
- Used for: CRUD operations, authentication, real-time data

### 2. Service-to-Service Communication
- **Quiz Service → Question Service**: Fetch questions for quiz
- **Quiz Service → Auth Service**: Validate user tokens (optional, can use JWT validation)

### 3. Future: Asynchronous Communication (Message Queue)
- Event-driven updates (e.g., quiz completion events)
- Consider: RabbitMQ, Apache Kafka for future scalability

## Authentication & Authorization

### Authentication Flow:
1. User submits credentials to `POST /api/auth/login`
2. Auth Service validates credentials
3. Auth Service generates JWT token
4. Token returned to frontend
5. Frontend includes token in `Authorization: Bearer <token>` header
6. API Gateway validates token (or forwards to Auth Service)
7. Request routed to appropriate service

### JWT Token Structure:
```json
{
  "sub": "user@example.com",
  "userId": 123,
  "roles": ["USER"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

### Security:
- All passwords hashed with BCrypt
- JWT tokens with expiration
- HTTPS in production
- CORS configured at API Gateway
- Rate limiting per user/IP

## Data Storage

### Development:
- H2 file-based databases for each service
- Data persisted in `./data/` directory
- Easy local development and testing

### Production Considerations:
- Migrate to PostgreSQL/MySQL for each service
- Each service has its own database (database per service pattern)
- No direct database access between services
- Data consistency via API calls or eventual consistency patterns

## Configuration

### Application Ports:
```properties
frontend:         5174 (dev), 3000 (prod)
api-gateway:      8080
auth-service:     8081
question-service: 8082
quiz-service:     8083
```

### Environment Variables:
Each service should support:
```properties
# Server
SERVER_PORT=808X
SPRING_PROFILES_ACTIVE=dev|prod

# Database
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/service.db
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=

# JWT (Auth Service)
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# Service Discovery (future)
EUREKA_SERVER_URL=http://localhost:8761/eureka
```

## API Documentation

### OpenAPI/Swagger:
- Each service exposes OpenAPI documentation
- Accessible at: `http://localhost:808X/swagger-ui.html`
- API Gateway aggregates all service APIs

### Adding OpenAPI to Services:
Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Development Workflow

### Running Locally:
1. Start Auth Service: `cd quizz-auth-service && ./mvnw spring-boot:run`
2. Start Question Service: `cd quizz-question-service && ./mvnw spring-boot:run`
3. Start Quiz Service: `cd quizz-quiz-service && ./mvnw spring-boot:run`
4. Start API Gateway: `cd quizz-api-gateway && ./mvnw spring-boot:run`
5. Start Frontend: `cd quizz_frontend && npm run dev`

### Testing Services:
```bash
# Health check
curl http://localhost:8081/actuator/health

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123"}'

# Create question
curl -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"text":"What is 2+2?","type":"MULTIPLE_CHOICE","category":"Math"}'
```

## Future Enhancements

### 1. Service Discovery
- Add Eureka Server for dynamic service discovery
- Services register themselves at startup
- API Gateway discovers services dynamically

### 2. Centralized Configuration
- Spring Cloud Config Server
- Externalized configuration for all services
- Environment-specific configurations

### 3. Distributed Tracing
- Spring Cloud Sleuth + Zipkin
- Track requests across services
- Performance monitoring

### 4. Circuit Breaker
- Resilience4j for fault tolerance
- Graceful degradation when services fail
- Fallback mechanisms

### 5. Message Queue
- RabbitMQ or Apache Kafka
- Asynchronous event processing
- Decoupled service communication

### 6. Caching
- Redis for distributed caching
- Cache frequently accessed data
- Reduce database load

### 7. Containerization
- Docker containers for each service
- Docker Compose for local orchestration
- Kubernetes for production deployment

## Best Practices

1. **Database per Service**: Each service owns its database
2. **API Versioning**: Use `/api/v1/` for future-proof APIs
3. **Error Handling**: Consistent error response format
4. **Logging**: Structured logging with correlation IDs
5. **Testing**: Unit tests, integration tests, contract tests
6. **Documentation**: Keep OpenAPI specs up to date
7. **Security**: Validate all inputs, use parameterized queries
8. **Monitoring**: Health checks, metrics, alerting

## Technology Stack Summary

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.0
- **Build Tool**: Maven
- **Database**: H2 (dev), PostgreSQL (prod)
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security, JWT
- **Documentation**: SpringDoc OpenAPI
- **Monitoring**: Spring Boot Actuator
- **Frontend**: React 18.3, Vite 6.x

## Repository Links

- Frontend: https://github.com/justinGirot/Quizz_frontend
- Auth Service: https://github.com/justinGirot/quizz-auth-service
- Question Service: https://github.com/justinGirot/quizz-question-service
- Quiz Service: https://github.com/justinGirot/quizz-quiz-service
- API Gateway: https://github.com/justinGirot/quizz-api-gateway

---

**Note**: This architecture is designed for scalability and maintainability. Start simple and add complexity as needed.
