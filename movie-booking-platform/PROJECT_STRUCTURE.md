# Movie Booking Platform - Project Structure

## Overview
This document describes the mono-repo multi-module Maven project structure for the XYZ Movie Booking Platform.

## Directory Structure

```
movie-booking-platform/
├── pom.xml                          # Parent POM with dependency management
├── docker-compose.yml               # Local development infrastructure
├── .gitignore                       # Git ignore rules for Java/Maven
├── .env.example                     # Environment variables template
├── README.md                        # Project documentation
│
├── services/                        # Microservices modules
│   ├── api-gateway/                 # Port 8080 - Routing, JWT, rate limiting
│   │   └── pom.xml
│   ├── auth-service/                # Port 8081 - Authentication & authorization
│   │   └── pom.xml
│   ├── theatre-service/             # Port 8082 - Theatre & show management
│   │   └── pom.xml
│   ├── booking-service/             # Port 8083 - Seat booking & reservation
│   │   └── pom.xml
│   ├── payment-service/             # Port 8084 - Payment processing
│   │   └── pom.xml
│   └── notification-service/        # Port 8085 - Async notifications
│       └── pom.xml
│
├── common/                          # Shared libraries
│   ├── domain-events/               # Kafka event definitions
│   │   ├── pom.xml
│   │   └── src/main/java/com/xyz/common/events/
│   │       ├── DomainEvent.java
│   │       ├── BookingConfirmedEvent.java
│   │       └── PaymentSuccessEvent.java
│   │
│   ├── shared-dtos/                 # Shared data transfer objects
│   │   ├── pom.xml
│   │   └── src/main/java/com/xyz/common/dto/
│   │       ├── ApiResponse.java
│   │       └── ErrorResponse.java
│   │
│   └── shared-utils/                # Common utilities
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/xyz/common/
│           │   ├── utils/
│           │   │   ├── CorrelationIdFilter.java
│           │   │   └── CorrelationIdInterceptor.java
│           │   └── exception/
│           │       ├── BaseException.java
│           │       ├── ValidationException.java
│           │       ├── UnauthorizedException.java
│           │       ├── ResourceNotFoundException.java
│           │       └── GlobalExceptionHandler.java
│           └── resources/
│               └── logback-spring.xml
│
└── docker/                          # Docker configuration
    └── init-db.sql                  # Database initialization script
```

## Technology Stack

### Core Technologies
- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Maven**: 3.8+

### Databases & Caching
- **PostgreSQL**: 42.7.1 (Multi-database setup)
- **Redis**: Redisson 3.25.2

### Messaging
- **Apache Kafka**: 3.6.1
- **Spring Kafka**: Included in Spring Boot

### Security
- **JWT**: JJWT 0.12.3 (RS256 signing)
- **Spring Security**: Included in Spring Boot

### Testing
- **JUnit**: Included in Spring Boot
- **jqwik**: 1.8.2 (Property-based testing)

### Observability
- **Logback**: Structured JSON logging
- **Logstash Encoder**: 7.4

## Infrastructure Services (Docker Compose)

### PostgreSQL
- **Image**: postgres:16-alpine
- **Port**: 5432
- **Databases**: auth_db, theatre_db, booking_db, payment_db
- **Health Check**: pg_isready

### Redis
- **Image**: redis:7-alpine
- **Port**: 6379
- **Persistence**: AOF enabled
- **Health Check**: redis-cli ping

### Kafka
- **Image**: confluentinc/cp-kafka:7.5.0
- **Ports**: 9092 (external), 29092 (internal)
- **Health Check**: kafka-broker-api-versions

### Zookeeper
- **Image**: confluentinc/cp-zookeeper:7.5.0
- **Port**: 2181
- **Health Check**: nc -z localhost 2181

## Common Modules

### 1. shared-utils
**Purpose**: Common utilities and cross-cutting concerns

**Features**:
- **Correlation ID Propagation**: Distributed tracing across services
  - `CorrelationIdFilter`: Servlet filter to extract/generate correlation IDs
  - `CorrelationIdInterceptor`: HTTP client interceptor to propagate IDs
- **Structured Logging**: JSON logs with MDC context
  - Correlation ID, User ID, Request ID
  - 30-day retention
  - Console and file appenders
- **Exception Handling**: Centralized error handling
  - `BaseException`: Abstract base for all business exceptions
  - `ValidationException`: 400 Bad Request
  - `UnauthorizedException`: 401 Unauthorized
  - `ResourceNotFoundException`: 404 Not Found
  - `GlobalExceptionHandler`: REST controller advice

### 2. shared-dtos
**Purpose**: Shared data transfer objects

**Features**:
- `ApiResponse<T>`: Generic API response wrapper
- `ErrorResponse`: Standardized error response format

### 3. domain-events
**Purpose**: Kafka event definitions for async communication

**Features**:
- `DomainEvent`: Base class for all events
- `BookingConfirmedEvent`: Published when booking is confirmed
- `PaymentSuccessEvent`: Published when payment succeeds

## Dependency Management

The parent POM manages versions for:
- Spring Boot and Spring Cloud
- PostgreSQL driver
- Redisson (Redis client)
- JJWT (JWT library)
- jqwik (Property-based testing)

All service modules inherit from the parent POM and can use managed dependencies without specifying versions.

## Build Commands

### Build All Modules
```bash
mvn clean install
```

### Build Specific Service
```bash
cd services/auth-service
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

## Development Workflow

1. **Start Infrastructure**: `docker-compose up -d`
2. **Build Project**: `mvn clean install`
3. **Run Services**: Each service can run independently with `mvn spring-boot:run`
4. **View Logs**: Check `logs/` directory for structured JSON logs
5. **Stop Infrastructure**: `docker-compose down`

## Observability Features

### Distributed Tracing
- Every request gets a unique correlation ID
- Correlation ID propagates across all service calls
- Logged in MDC context for easy tracking

### Structured Logging
- JSON format for easy parsing
- Includes: timestamp, level, logger, message, correlationId, userId
- Separate log files per service
- 30-day retention with daily rotation

### Health Checks
- All infrastructure services have health checks
- Services can implement Spring Boot Actuator for health endpoints

## Next Steps

After completing this infrastructure setup, the next tasks are:
1. Implement Auth Service (user registration, login, JWT)
2. Implement Theatre Service (theatre, screen, show management)
3. Implement Booking Service (seat hold, confirm, cancel)
4. Implement Payment Service (payment initiation, webhook)
5. Implement Notification Service (Kafka consumers)
6. Implement API Gateway (routing, JWT validation, rate limiting)
