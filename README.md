# XYZ Movie Booking Platform

A distributed microservices-based movie ticket booking platform built with Java 17, Spring Boot 3.x, PostgreSQL, Redis, Kafka, and AWS ECS Fargate.

## 📚 Documentation

### Core Specifications
- **[Requirements](/.kiro/specs/movie-booking-platform/requirements.md)** - 29 functional requirements, 48 non-functional requirements, user capacity planning
- **[Design](/.kiro/specs/movie-booking-platform/design.md)** - HLD, LLD, 15 design patterns, API contracts, 28 correctness properties
- **[Tasks](/.kiro/specs/movie-booking-platform/tasks.md)** - 180+ implementation tasks with property-based testing

### Architecture Guides
- **[Architecture Summary](/ARCHITECTURE_SUMMARY.md)** - Quick reference for patterns, requirements, capacity
- **[Availability & Consistency](/AVAILABILITY_CONSISTENCY.md)** - Service availability targets and consistency models
- **[Database Scaling Guide](/DATABASE_SCALING_GUIDE.md)** - How to scale beyond 1TB (myth busted!)
- **[Repository Structure](/REPOSITORY_STRUCTURE.md)** - Mono-repo structure, build commands, CI/CD
- **[Mono vs Multi-repo](/MONO_VS_MULTI_REPO.md)** - Repository strategy decision guide

## 🎯 Key Features

### B2B (Theatre Partners)
- Theatre onboarding and approval workflow
- Screen and show management
- Seat inventory allocation and bulk updates

### B2C (End Customers)
- Browse movies by city, language, genre
- Real-time seat availability with distributed locking
- Booking with offer engine (50% off 3rd ticket, 20% afternoon discount)
- Payment processing and refunds
- Booking confirmation notifications

## 🏗️ Architecture

### Microservices (6 services)
1. **API Gateway** - Routing, JWT auth, rate limiting (99.99% availability)
2. **Auth Service** - Registration, login, JWT, RBAC (99.99% availability, eventual consistency)
3. **Theatre Service** - Theatre/show/seat CRUD (99.95% availability, eventual consistency)
4. **Booking Service** - Seat hold, confirm, cancel (99.99% availability, **strong consistency**)
5. **Payment Service** - Payment initiation, webhook (99.95% availability, eventual consistency)
6. **Notification Service** - Email/SMS async (99.9% availability, eventual consistency)

### Design Patterns (15 patterns)
- **Creational**: Factory, Builder
- **Structural**: Repository, Adapter, Facade, Proxy/Gateway
- **Behavioral**: Strategy, Observer, Template Method, Chain of Responsibility, Command
- **Concurrency**: Distributed Lock, Circuit Breaker
- **Architectural**: Saga, CQRS, Event Sourcing

### Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL 15 (Multi-AZ)
- **Cache**: Redis 7.x (Cluster mode)
- **Message Broker**: Kafka 3.5 (MSK)
- **Deployment**: AWS ECS Fargate
- **Monitoring**: CloudWatch, X-Ray

## 📊 Capacity Planning

### Target Users
- Year 1: 1M registered, 200K active monthly
- Year 2: 5M registered, 1M active monthly
- Year 3: 10M registered, 3M active monthly

### Traffic Patterns
- **Normal**: 10,000 concurrent users, 500 bookings/min
- **Peak**: 100,000 concurrent users, 5,000 bookings/min (10x)

### Cost Estimate
- Normal load: $2,154/month
- Peak load: $3,079/month
- Annual average: $2,394/month

## 🚀 Getting Started

### Prerequisites
- Java 17
- Maven 3.8+
- Docker & Docker Compose

### Local Development

```bash
# Clone repository
git clone <your-repo-url>
cd movie-booking-platform

# Start infrastructure (PostgreSQL, Redis, Kafka)
cd infrastructure/docker
docker-compose up -d

# Build all services
cd ../..
mvn clean install

# Run services
cd services/auth-service && mvn spring-boot:run
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run property-based tests
mvn test -Dtest=**/*PropertyTest
```

## 🔒 Security

- JWT authentication (RS256)
- RBAC authorization
- Rate limiting (100 req/min)
- OWASP Top 10 compliance
- PCI-DSS, GDPR compliance

## 📈 Monitoring

- CloudWatch Logs & Metrics
- AWS X-Ray distributed tracing
- Custom business metrics
- PagerDuty & Slack alerting

---

**Status**: Complete specifications, ready for implementation
**Last Updated**: 2024-01-15
