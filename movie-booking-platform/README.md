# XYZ Movie Booking Platform

A distributed microservices-based movie ticket booking platform built with Java 17, Spring Boot 3.x, PostgreSQL, Redis, and Apache Kafka.

## Architecture

The platform consists of 6 microservices:
- **API Gateway** (Port 8080) - Routing, JWT validation, rate limiting
- **Auth Service** (Port 8081) - User authentication and authorization
- **Theatre Service** (Port 8082) - Theatre and show management
- **Booking Service** (Port 8083) - Seat booking and reservation
- **Payment Service** (Port 8084) - Payment processing
- **Notification Service** (Port 8085) - Async notifications via Kafka

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose

## Getting Started

### 1. Start Infrastructure Services

```bash
cd movie-booking-platform
docker-compose up -d
```

This will start:
- PostgreSQL (Port 5432)
- Redis (Port 6379)
- Kafka (Port 9092)
- Zookeeper (Port 2181)

### 2. Build All Services

```bash
mvn clean install
```

### 3. Run Individual Services

Each service can be run independently:

```bash
# API Gateway
cd services/api-gateway
mvn spring-boot:run

# Auth Service
cd services/auth-service
mvn spring-boot:run

# Theatre Service
cd services/theatre-service
mvn spring-boot:run

# Booking Service
cd services/booking-service
mvn spring-boot:run

# Payment Service
cd services/payment-service
mvn spring-boot:run

# Notification Service
cd services/notification-service
mvn spring-boot:run
```

## Project Structure

```
movie-booking-platform/
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── theatre-service/
│   ├── booking-service/
│   ├── payment-service/
│   └── notification-service/
├── common/
│   ├── domain-events/      # Kafka event definitions
│   ├── shared-dtos/        # Shared data transfer objects
│   └── shared-utils/       # Common utilities and exceptions
├── docker-compose.yml
└── pom.xml
```

## Common Modules

### shared-utils
- Correlation ID propagation for distributed tracing
- Structured JSON logging with Logback
- Global exception handling
- Common exception classes

### shared-dtos
- API response wrappers
- Error response DTOs

### domain-events
- Kafka event definitions
- Domain event base classes

## Configuration

Copy `.env.example` to `.env` and update the values:

```bash
cp .env.example .env
```

## Health Checks

All infrastructure services include health checks:
- PostgreSQL: `pg_isready`
- Redis: `redis-cli ping`
- Kafka: `kafka-broker-api-versions`
- Zookeeper: `nc -z localhost 2181`

## Observability

The platform includes:
- **Distributed Tracing**: Correlation ID propagation across all services
- **Structured Logging**: JSON logs with correlation IDs, user IDs, and request IDs
- **Log Retention**: 30 days (configurable in logback-spring.xml)

## Development

### Running Tests

```bash
mvn test
```

### Building Docker Images

```bash
mvn spring-boot:build-image
```

## License

Proprietary - XYZ Corporation
