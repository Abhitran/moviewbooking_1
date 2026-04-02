# XYZ Movie Booking Platform - Architecture Summary

## Design Patterns Used (15 Patterns)

### Creational Patterns
1. **Factory Pattern** - Notification sender creation (Email/SMS/Push)
2. **Builder Pattern** - Complex DTO construction

### Structural Patterns
3. **Repository Pattern** - Data access abstraction (all services)
4. **Adapter Pattern** - Payment gateway integration
5. **Facade Pattern** - Booking workflow simplification
6. **Proxy/Gateway Pattern** - API Gateway for cross-cutting concerns

### Behavioral Patterns
7. **Strategy Pattern** - Offer engine (discount algorithms)
8. **Observer Pattern** - Event-driven architecture (Kafka)
9. **Template Method Pattern** - Authentication flow
10. **Chain of Responsibility** - API Gateway filters
11. **Command Pattern** - Booking operations encapsulation

### Concurrency Patterns
12. **Distributed Lock Pattern** - Seat booking (Redis locks)
13. **Circuit Breaker Pattern** - Fault tolerance (Resilience4j)

### Architectural Patterns
14. **Saga Pattern** - Distributed transactions with compensation
15. **CQRS (Partial)** - Read/write separation for scalability

---

## Functional Requirements Summary

### Auth Service (5 requirements)
- FR-AUTH-01 to FR-AUTH-05: Registration, login, JWT, RBAC, logout
- **Availability**: 99.99% | **Consistency**: Eventual (Redis cache)

### Theatre Service (6 requirements)
- FR-TH-01 to FR-TH-06: Theatre onboarding, screens, shows, seats, approval
- **Availability**: 99.95% | **Consistency**: Eventual (Read-heavy, 60s cache)

### Booking Service (9 requirements)
- FR-BK-01 to FR-BK-09: Browse, seat hold, confirm, cancel, bulk ops, offers
- **Availability**: 99.99% | **Consistency**: **STRONG** (No double booking)

### Payment Service (5 requirements)
- FR-PAY-01 to FR-PAY-05: Payment initiation, webhook, refund
- **Availability**: 99.95% | **Consistency**: Eventual (Async events, 1-5s delay)

### Search/Browse (4 requirements)
- FR-SR-01 to FR-SR-04: Movie/theatre search, show timings, offers
- **Availability**: 99.95% | **Consistency**: Eventual (Part of Theatre Service)

**Total: 29 Functional Requirements**

---

## Service Availability & Consistency Matrix

| Service | Availability | Consistency | CAP Choice | Why |
|---|---|---|---|---|
| API Gateway | 99.99% | Stateless | N/A | Entry point |
| Auth Service | 99.99% | Eventual | AP | Fast auth > perfect consistency |
| Theatre Service | 99.95% | Eventual | AP | Search must work > accuracy |
| **Booking Service** | 99.99% | **Strong** | **CP** | **No double booking allowed** |
| Payment Service | 99.95% | Eventual | AP | Retry webhooks > immediate |
| Notification Service | 99.9% | Eventual | AP | Best effort delivery |

**Key Insight**: Only Booking Service uses strong consistency (CP) to prevent double booking. All other services use eventual consistency (AP) for better performance and availability.

---

## Non-Functional Requirements Summary

### Performance (7 NFRs)
- Browse APIs: < 200ms p99
- Booking hold: < 500ms p99
- Redis cache hit ratio: > 80%

### Scalability (7 NFRs)
- Peak concurrent users: 100,000
- Peak bookings: 5,000/min
- API throughput: 10,000 RPS

### Availability (8 NFRs)
- System uptime: 99.99%
- Multi-AZ deployment
- RTO: < 2 minutes

### Security (10 NFRs)
- JWT authentication (RS256)
- RBAC authorization
- OWASP Top 10 compliance
- PCI-DSS, GDPR compliance

### Observability (8 NFRs)
- Centralized logging (CloudWatch)
- Distributed tracing (X-Ray)
- Real-time metrics and alerting

**Total: 48 Non-Functional Requirements**

---

## User Capacity Planning

### Target Users
- Year 1: 1M registered, 200K active monthly
- Year 2: 5M registered, 1M active monthly
- Year 3: 10M registered, 3M active monthly

### Traffic Patterns
- Normal: 10,000 concurrent users, 500 bookings/min
- Peak: 100,000 concurrent users, 5,000 bookings/min (10x)

### Infrastructure Sizing
- ECS Tasks: 13 normal → 63 peak
- Database: db.r6g.xlarge (4 vCPU, 32 GB RAM)
- Redis: 6 nodes, 78 GB total memory
- Kafka: 3 brokers, 300 GB storage

### Cost Estimate
- Normal load: $2,154/month
- Peak load: $3,079/month
- Annual average: $2,394/month (~$28,728/year)

---

## Technology Stack
- Language: Java 17
- Framework: Spring Boot 3.x
- Database: PostgreSQL 15 (Multi-AZ)
- Cache: Redis 7.x (Cluster mode)
- Message Broker: Kafka 3.5 (MSK)
- Deployment: AWS ECS Fargate
- Monitoring: CloudWatch, X-Ray
- CI/CD: GitHub Actions

---

**Document Status**: Complete and ready for implementation
**Last Updated**: 2024-01-15
