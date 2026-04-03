# Movie Booking Platform

A distributed microservices-based movie ticket booking platform built with Spring Boot, designed for high scalability and reliability.

## Overview

This platform provides a complete movie ticket booking system with the following key features:
- User authentication and authorization
- Movie theatre management
- Seat booking and reservation
- Payment processing
- Notification system
- API Gateway for service orchestration

## Architecture

The application follows a microservices architecture with the following services:

### Core Services
- **API Gateway**: Entry point for all client requests, handles routing and authentication
- **Auth Service**: Manages user authentication and JWT token generation
- **Booking Service**: Handles seat reservations and booking logic
- **Theatre Service**: Manages theatre, movie, and showtime information
- **Payment Service**: Processes payments and integrates with payment gateways
- **Notification Service**: Sends email/SMS notifications for bookings and updates

### Shared Modules
- **Domain Events**: Common event definitions for inter-service communication
- **Shared DTOs**: Data transfer objects used across services
- **Shared Utils**: Common utilities and configurations

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **Containerization**: Docker & Docker Compose
- **API Documentation**: SpringDoc OpenAPI

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- Git

## Quick Start

1. **Clone the repository**
   ```bash
   git clone https://github.com/Abhitran/moviewbooking_1.git
   cd moviewbooking_1
   ```

2. **Start infrastructure services**
   ```bash
   cd movie-booking-platform
   docker-compose up -d
   ```

3. **Build the application**
   ```bash
   mvn clean install
   ```

4. **Run services**
   Each service can be started individually:
   ```bash
   # API Gateway
   cd services/api-gateway
   mvn spring-boot:run

   # Auth Service
   cd services/auth-service
   mvn spring-boot:run

   # And so on for other services...
   ```

## Configuration

The application uses Spring profiles for different environments. Key configuration files:

- `application.yml`: Common configuration
- `application-dev.yml`: Development environment
- `application-prod.yml`: Production environment

Environment variables for database and external services are defined in `docker-compose.yml`.

## API Documentation

Once the API Gateway is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Testing

The project includes Postman collections for API testing:
- `movie-booking-platform.postman_collection.json`
- `auth-service.postman_collection.json`

## Database Schema

The initial database schema is defined in `docker/init-db.sql`. The application uses Flyway for database migrations.

## Monitoring & Logging

- Application logs are configured using Logback
- Health checks are available at `/actuator/health` endpoints
- Metrics can be accessed via `/actuator/metrics`

## Development

### Code Structure
```
movie-booking-platform/
├── common/                 # Shared modules
│   ├── domain-events/      # Event definitions
│   ├── shared-dtos/        # DTOs
│   └── shared-utils/       # Utilities
├── services/              # Microservices
│   ├── api-gateway/       # API Gateway service
│   ├── auth-service/      # Authentication service
│   ├── booking-service/   # Booking management
│   ├── notification-service/ # Notification service
│   ├── payment-service/   # Payment processing
│   └── theatre-service/   # Theatre management
└── docker/                # Docker configurations
    ├── init-db.sql       # Database initialization
    └── services/         # Service Dockerfiles
```

### Adding New Features
1. Create feature branch from `main`
2. Implement changes following the existing patterns
3. Add tests for new functionality
4. Update documentation if needed
5. Create pull request

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.