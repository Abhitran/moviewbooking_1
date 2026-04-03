# Implementation Plan: XYZ Movie Booking Platform

## Overview

This implementation plan covers the development of a distributed microservices-based movie ticket booking platform using Java 17, Spring Boot 3.x, PostgreSQL, Redis, Kafka, and AWS ECS Fargate. The system consists of 6 microservices (api-gateway, auth-service, theatre-service, booking-service, payment-service, notification-service) with comprehensive testing including 28 property-based tests.

## Tasks

- [x] 1. Project setup and infrastructure foundation
  - [x] 1.1 Create mono-repo multi-module Maven project structure
    - Create root directory: `movie-booking-platform/`
    - Create parent POM with dependency management for all 6 services
    - Configure Spring Boot 3.x, Java 17, and common dependencies
    - Set up module structure:
      - `services/api-gateway/`
      - `services/auth-service/`
      - `services/theatre-service/`
      - `services/booking-service/`
      - `services/payment-service/`
      - `services/notification-service/`
      - `common/domain-events/`
      - `common/shared-dtos/`
      - `common/shared-utils/`
    - Create `.gitignore` for Java/Maven projects
    - _Requirements: All services_
    - _Note: Mono-repo approach for easier assignment demonstration_

  - [x] 1.2 Set up local development environment with Docker Compose
    - Create docker-compose.yml with PostgreSQL, Redis, Kafka/Zookeeper
    - Configure environment variables and network settings
    - Add health checks for all containers
    - _Requirements: NFR-Observability_

  - [x] 1.3 Configure shared libraries and common utilities
    - Create common module with shared DTOs, exceptions, and utilities
    - Implement correlation ID propagation for distributed tracing
    - Set up structured JSON logging with Logback
    - _Requirements: NFR-Observability_

- [x] 2. Implement Auth Service
  - [x] 2.1 Set up Auth Service project structure and dependencies
    - Configure Spring Boot with Spring Security, Spring Data JPA, Redis
    - Add JWT dependencies (jjwt-api 0.12.3)
    - Set up PostgreSQL connection and Flyway migrations
    - _Requirements: FR-AUTH-01 to FR-AUTH-05_


  - [x] 2.2 Create database schema and entities for Auth Service
    - Create Flyway migration for users table with UUID, email, password_hash, role, timestamps
    - Implement User entity with JPA annotations
    - Create UserRepository extending JpaRepository
    - _Requirements: FR-AUTH-01_

  - [x] 2.3 Implement user registration endpoint
    - Create RegisterRequest/Response DTOs
    - Implement UserService with BCrypt password hashing (strength 12)
    - Create AuthController with POST /api/auth/register endpoint
    - Add email validation and role assignment logic
    - _Requirements: FR-AUTH-01_

  - [ ]* 2.4 Write property test for user registration
    - **Property 1: User registration creates retrievable account**
    - **Validates: Requirements FR-AUTH-01**

  - [x] 2.5 Implement JWT token generation and login endpoint
    - Configure RS256 key pair for JWT signing
    - Create JwtService for token generation (access: 15min, refresh: 7 days)
    - Implement POST /api/auth/login endpoint with credential validation
    - Return LoginResponse with accessToken, refreshToken, expiresIn
    - _Requirements: FR-AUTH-02_

  - [ ]* 2.6 Write property test for login with valid credentials
    - **Property 2: Login with valid credentials returns valid tokens**
    - **Validates: Requirements FR-AUTH-02**

  - [x] 2.7 Implement role-based access control (RBAC)
    - Create custom JWT authentication filter
    - Implement role extraction from JWT claims
    - Add method-level security annotations (@PreAuthorize)
    - Configure security filter chain with role-based rules
    - _Requirements: FR-AUTH-03_

  - [ ]* 2.8 Write property test for RBAC enforcement
    - **Property 3: Role-based access control enforcement**
    - **Validates: Requirements FR-AUTH-03**

  - [x] 2.9 Implement token refresh endpoint
    - Create POST /api/auth/refresh endpoint
    - Validate refresh token and generate new access token
    - Maintain same user claims in refreshed token
    - _Requirements: FR-AUTH-04_

  - [ ]* 2.10 Write property test for token refresh
    - **Property 4: Token refresh produces valid access token**
    - **Validates: Requirements FR-AUTH-04**

  - [x] 2.11 Implement logout with token blacklisting
    - Create POST /api/auth/logout endpoint
    - Store blacklisted tokens in Redis with TTL matching token expiry
    - Update JWT filter to check blacklist before validating token
    - _Requirements: FR-AUTH-05_

  - [ ]* 2.12 Write property test for logout token invalidation
    - **Property 5: Logged-out tokens are rejected**
    - **Validates: Requirements FR-AUTH-05**

  - [x] 2.13 Implement token validation endpoint
    - Create GET /api/auth/validate endpoint
    - Return userId, role, and validity status
    - Used by API Gateway for authentication
    - _Requirements: FR-AUTH-03_

  - [ ]* 2.14 Write unit tests for Auth Service
    - Test invalid credentials, weak passwords, duplicate email registration
    - Test expired token handling, invalid token format
    - Test edge cases for token refresh and blacklisting

- [x] 3. Implement Theatre Service
  - [x] 3.1 Set up Theatre Service project structure and dependencies
    - Configure Spring Boot with Spring Data JPA, Redis for caching
    - Set up PostgreSQL connection and Flyway migrations
    - Add RestTemplate/WebClient for inter-service communication
    - _Requirements: FR-TH-01 to FR-TH-06_

  - [x] 3.2 Create database schemas for Theatre Service
    - Create Flyway migrations for theatres, screens, shows, seats tables
    - Add indexes: idx_theatres_city, idx_theatres_partner, idx_shows_movie, idx_seats_show
    - Implement foreign key constraints and cascading deletes
    - _Requirements: FR-TH-01 to FR-TH-04_

  - [x] 3.3 Create JPA entities and repositories
    - Implement Theatre, Screen, Show, Seat entities with relationships
    - Create repositories: TheatreRepository, ScreenRepository, ShowRepository, SeatRepository
    - Add custom query methods for search operations
    - _Requirements: FR-TH-01 to FR-TH-04_

  - [x] 3.4 Implement theatre registration and onboarding
    - Create POST /api/theatres endpoint with TheatreRequest DTO
    - Implement TheatreService to create theatre with PENDING_APPROVAL status
    - Create screens and initial seat layout in same transaction
    - Validate theatre partner role before allowing creation
    - _Requirements: FR-TH-01_

  - [ ]* 3.5 Write property test for theatre creation
    - **Property 6: Theatre creation is retrievable**
    - **Validates: Requirements FR-TH-01**

  - [x] 3.6 Implement screen management endpoints
    - Create POST /api/theatres/{theatreId}/screens for adding screens
    - Create PUT /api/theatres/{theatreId}/screens/{screenId} for updates
    - Create DELETE /api/theatres/{theatreId}/screens/{screenId} for deletion
    - Validate ownership before allowing modifications
    - _Requirements: FR-TH-02_

  - [ ]* 3.7 Write property test for screen operations
    - **Property 7: Screen operations maintain count invariant**
    - **Validates: Requirements FR-TH-02**

  - [x] 3.8 Implement show management endpoints
    - Create POST /api/theatres/{theatreId}/shows for creating shows
    - Create PUT /api/theatres/shows/{showId} for updates
    - Create DELETE /api/theatres/shows/{showId} for deletion
    - Add unique constraint validation for (screen_id, show_date, show_time)
    - _Requirements: FR-TH-03_

  - [ ]* 3.9 Write property test for show CRUD operations
    - **Property 8: Show CRUD maintains data integrity**
    - **Validates: Requirements FR-TH-03**

  - [x] 3.10 Implement seat inventory allocation
    - Create seat records for each show based on screen layout
    - Implement POST endpoint to initialize seats with AVAILABLE/BLOCKED status
    - Ensure each seat number appears exactly once per show
    - _Requirements: FR-TH-04_

  - [ ]* 3.11 Write property test for seat allocation
    - **Property 9: Seat allocation creates correct inventory**
    - **Validates: Requirements FR-TH-04**

  - [x] 3.12 Implement bulk seat status update
    - Create PUT /api/theatres/shows/{showId}/seats endpoint
    - Accept array of seat updates with seat number and status
    - Execute updates in single transaction (atomic operation)
    - _Requirements: FR-TH-05_

  - [ ]* 3.13 Write property test for bulk seat update atomicity
    - **Property 10: Bulk seat update is atomic**
    - **Validates: Requirements FR-TH-05**

  - [x] 3.14 Implement theatre approval workflow
    - Create PUT /api/theatres/{theatreId}/approve endpoint (SUPER_ADMIN only)
    - Transition status from PENDING_APPROVAL to APPROVED or REJECTED
    - Validate current status before allowing transition
    - _Requirements: FR-TH-06_

  - [ ]* 3.15 Write property test for theatre approval
    - **Property 11: Theatre approval transitions status correctly**
    - **Validates: Requirements FR-TH-06**

  - [x] 3.16 Implement theatre and show search endpoints
    - Create GET /api/theatres/search with filters: city, movieName, date, language, genre
    - Return theatres with matching shows and available seat counts
    - Implement caching with Redis (60-second TTL)
    - _Requirements: FR-BK-01, FR-SR-01, FR-SR-02, FR-SR-03_

  - [ ]* 3.17 Write property test for search filter matching
    - **Property 12: Search results match filter criteria**
    - **Validates: Requirements FR-BK-01, FR-SR-01, FR-SR-02, FR-SR-03**

  - [ ]* 3.18 Write unit tests for Theatre Service
    - Test duplicate show time conflict (409 error)
    - Test invalid seat layout handling
    - Test unauthorized access to partner-only endpoints
    - Test theatre approval by non-admin user

- [ ] 4. Checkpoint - Verify Auth and Theatre services
  - Ensure all tests pass for Auth and Theatre services
  - Verify database migrations applied correctly
  - Test inter-service communication patterns
  - Ask the user if questions arise


- [-] 5. Implement Booking Service core functionality
  - [x] 5.1 Set up Booking Service project structure and dependencies
    - Configure Spring Boot with Spring Data JPA, Redis (Redisson), Kafka
    - Add Redisson dependency (3.24.3) for distributed locking
    - Set up PostgreSQL connection and Flyway migrations
    - Configure Kafka producer for booking events
    - _Requirements: FR-BK-01 to FR-BK-09_

  - [x] 5.2 Create database schemas for Booking Service
    - Create Flyway migrations for bookings and booking_seats tables
    - Add indexes: idx_bookings_user, idx_bookings_show, idx_bookings_status
    - Implement foreign key constraints
    - _Requirements: FR-BK-01 to FR-BK-09_

  - [x] 5.3 Create JPA entities and repositories
    - Implement Booking and BookingSeat entities with relationships
    - Create BookingRepository with custom query methods
    - Add methods for finding by userId, showId, and status
    - _Requirements: FR-BK-01 to FR-BK-09_

  - [ ] 5.4 Implement seat availability query endpoint
    - Create GET /api/bookings/show/{showId}/seats endpoint
    - Query seat status from Theatre Service
    - Check Redis for held seats (not yet expired)
    - Return available, held, and booked seat lists
    - _Requirements: FR-BK-02_

  - [ ]* 5.5 Write property test for available seats query
    - **Property 13: Available seats query returns only bookable seats**
    - **Validates: Requirements FR-BK-02**

  - [ ] 5.6 Implement seat hold with distributed locking
    - Create POST /api/bookings/hold endpoint
    - Validate seat count <= 10 seats
    - Use Redisson distributed locks with 10-minute TTL
    - Store locks in Redis: seat:lock:{showId}:{seatNumber} = {userId}
    - Return holdId, expiresAt, and seat list
    - _Requirements: FR-BK-03_

  - [ ]* 5.7 Write property test for seat hold limit
    - **Property 14: Seat hold respects maximum limit**
    - **Validates: Requirements FR-BK-03**

  - [ ]* 5.8 Write property test for seat lock expiration
    - **Property 15: Seat locks expire after TTL**
    - **Validates: Requirements FR-BK-03**

  - [ ]* 5.9 Write property test for no double booking
    - **Property 16: No double booking of seats**
    - **Validates: Requirements FR-BK-03, NFR-Consistency**

  - [x] 5.10 Implement offer engine with Strategy pattern
    - Create OfferStrategy interface with apply(BookingContext) method
    - Implement ThirdTicketDiscountStrategy (50% off 3rd ticket)
    - Implement AfternoonShowDiscountStrategy (20% off for 12:00-17:00 shows)
    - Create OfferEngine to evaluate all strategies and select best discount
    - _Requirements: FR-BK-08_

  - [ ]* 5.11 Write property test for third ticket discount
    - **Property 21: Third ticket discount applied correctly**
    - **Validates: Requirements FR-BK-08**

  - [ ]* 5.12 Write property test for afternoon show discount
    - **Property 22: Afternoon show discount applied correctly**
    - **Validates: Requirements FR-BK-08**

  - [ ]* 5.13 Write property test for offer mutual exclusivity
    - **Property 23: Offer mutual exclusivity**
    - **Validates: Requirements FR-BK-08**

  - [x] 5.14 Implement booking confirmation endpoint
    - Create POST /api/bookings/confirm endpoint
    - Validate holdId exists and not expired
    - Calculate offers using OfferEngine
    - Call Payment Service to initiate payment
    - Create booking record with PENDING status
    - _Requirements: FR-BK-04, FR-BK-08_

  - [ ]* 5.15 Write property test for payment success confirmation
    - **Property 17: Payment success confirms booking**
    - **Validates: Requirements FR-BK-04, FR-PAY-03**

  - [x] 5.16 Implement Kafka consumer for payment events
    - Create @KafkaListener for payment.success topic
    - Update booking status to CONFIRMED
    - Release seat locks from Redis
    - Publish booking.confirmed event to Kafka
    - _Requirements: FR-BK-04, FR-PAY-03_

  - [ ]* 5.17 Write property test for booking confirmation notification
    - **Property 24: Booking confirmation publishes notification event**
    - **Validates: Requirements FR-BK-09**

  - [x] 5.18 Implement booking cancellation endpoint
    - Create DELETE /api/bookings/{bookingId} endpoint
    - Validate booking exists and status is CONFIRMED
    - Update booking status to CANCELLED
    - Call Payment Service to initiate refund
    - Publish booking.cancelled event to Kafka
    - _Requirements: FR-BK-05_

  - [ ]* 5.19 Write property test for cancellation
    - **Property 18: Cancellation transitions status and initiates refund**
    - **Validates: Requirements FR-BK-05, FR-PAY-05**

  - [x] 5.20 Implement bulk booking endpoint
    - Create POST /api/bookings/bulk endpoint
    - Accept multiple seat selections in single request
    - Execute all seat locks and booking creation in single transaction
    - Rollback all changes if any seat fails
    - _Requirements: FR-BK-06_

  - [ ]* 5.21 Write property test for bulk booking atomicity
    - **Property 19: Bulk booking is atomic**
    - **Validates: Requirements FR-BK-06**

  - [x] 5.22 Implement bulk cancellation endpoint
    - Create DELETE /api/bookings/bulk endpoint
    - Accept array of booking IDs
    - Cancel all bookings in single transaction
    - Rollback if any cancellation fails
    - _Requirements: FR-BK-07_

  - [ ]* 5.23 Write property test for bulk cancellation atomicity
    - **Property 20: Bulk cancellation is atomic**
    - **Validates: Requirements FR-BK-07**

  - [x] 5.24 Implement user bookings query endpoint
    - Create GET /api/bookings/user/{userId} endpoint
    - Return all bookings with show details, seats, amounts, status
    - Add pagination support (page size: 20)
    - _Requirements: FR-BK-01_

  - [ ]* 5.25 Write unit tests for Booking Service
    - Test hold expiration handling
    - Test booking with 0 seats (validation error)
    - Test cancellation of non-existent booking
    - Test concurrent booking attempts on same seat

- [ ] 6. Implement Payment Service
  - [x] 6.1 Set up Payment Service project structure and dependencies
    - Configure Spring Boot with Spring Data JPA, Kafka
    - Set up PostgreSQL connection and Flyway migrations
    - Configure Kafka producer for payment events
    - _Requirements: FR-PAY-01 to FR-PAY-05_

  - [x] 6.2 Create database schemas for Payment Service
    - Create Flyway migrations for payments and refunds tables
    - Add indexes: idx_payments_booking, idx_payments_user, idx_refunds_payment
    - Implement foreign key constraints
    - _Requirements: FR-PAY-01 to FR-PAY-05_

  - [x] 6.3 Create JPA entities and repositories
    - Implement Payment and Refund entities
    - Create PaymentRepository and RefundRepository
    - Add custom query methods for finding by bookingId and userId
    - _Requirements: FR-PAY-01 to FR-PAY-05_

  - [x] 6.4 Implement payment initiation endpoint
    - Create POST /api/payments/initiate endpoint
    - Create payment record with PENDING status
    - Generate stubbed gateway URL
    - Return paymentId and gatewayUrl
    - _Requirements: FR-PAY-01_

  - [ ]* 6.5 Write property test for payment initiation
    - **Property 25: Payment initiation creates pending record**
    - **Validates: Requirements FR-PAY-01**

  - [x] 6.6 Implement payment gateway webhook handler
    - Create POST /api/payments/webhook endpoint
    - Validate webhook payload (idempotency check)
    - Update payment status to SUCCESS or FAILED
    - Store gatewayTransactionId
    - _Requirements: FR-PAY-02_

  - [ ]* 6.7 Write property test for webhook status update
    - **Property 26: Payment webhook updates payment status**
    - **Validates: Requirements FR-PAY-02**

  - [x] 6.8 Implement payment success event publishing
    - Publish payment.success event to Kafka on successful payment
    - Include paymentId, bookingId, amount, gatewayTransactionId in event
    - _Requirements: FR-PAY-03_

  - [x] 6.9 Implement payment failure event publishing
    - Publish payment.failed event to Kafka on failed payment
    - Include paymentId, bookingId, reason in event
    - Trigger seat lock release in Booking Service
    - _Requirements: FR-PAY-04_

  - [ ]* 6.10 Write property test for payment failure seat release
    - **Property 27: Payment failure releases seat locks**
    - **Validates: Requirements FR-PAY-04**

  - [x] 6.11 Implement refund initiation endpoint
    - Create POST /api/payments/refund endpoint
    - Validate payment exists and is successful
    - Create refund record with PENDING status
    - Stub refund processing (immediate success)
    - _Requirements: FR-PAY-05_

  - [x] 6.12 Implement payment query endpoint
    - Create GET /api/payments/{paymentId} endpoint
    - Return payment details with status and timestamps
    - _Requirements: FR-PAY-01_

  - [ ]* 6.13 Write unit tests for Payment Service
    - Test webhook with invalid signature
    - Test duplicate webhook processing (idempotency)
    - Test refund for non-existent payment
    - Test payment status transitions

- [x] 7. Implement Notification Service
  - [x] 7.1 Set up Notification Service project structure and dependencies
    - Configure Spring Boot with Spring Kafka
    - Add email/SMS stub implementations (console logging)
    - Configure Kafka consumers for notification events
    - _Requirements: FR-BK-09_

  - [x] 7.2 Implement Kafka consumer for booking.confirmed events
    - Create @KafkaListener for booking.confirmed topic
    - Extract booking details from event payload
    - Send confirmation email/SMS (stubbed - log to console)
    - _Requirements: FR-BK-09_

  - [x] 7.3 Implement Kafka consumer for booking.cancelled events
    - Create @KafkaListener for booking.cancelled topic
    - Extract cancellation details from event payload
    - Send cancellation notification (stubbed - log to console)
    - _Requirements: FR-BK-09_

  - [x] 7.4 Implement Kafka consumer for payment.failed events
    - Create @KafkaListener for payment.failed topic
    - Extract payment failure details from event payload
    - Send payment failure alert (stubbed - log to console)
    - _Requirements: FR-PAY-04_

  - [ ]* 7.5 Write unit tests for Notification Service
    - Test Kafka message consumption and parsing
    - Test notification formatting
    - Test error handling for malformed events

- [x] 8. Implement API Gateway
  - [x] 8.1 Set up API Gateway project with Spring Cloud Gateway
    - Configure Spring Cloud Gateway dependencies
    - Set up routing configuration for all 5 backend services
    - Configure service discovery (if using Cloud Map) or direct URLs
    - _Requirements: All services_

  - [x] 8.2 Implement JWT authentication filter
    - Create global filter to validate JWT tokens
    - Call Auth Service /api/auth/validate endpoint
    - Extract userId and role from token
    - Add user context to request headers for downstream services
    - _Requirements: FR-AUTH-03, NFR-Security_

  - [x] 8.3 Implement role-based routing and authorization
    - Configure route-level role requirements
    - Block requests that don't meet role requirements (403 Forbidden)
    - Allow public routes (login, register) without authentication
    - _Requirements: FR-AUTH-03_

  - [x] 8.4 Implement rate limiting
    - Configure Token Bucket rate limiter (100 req/min per user)
    - Use Redis for distributed rate limit tracking
    - Return 429 Too Many Requests when limit exceeded
    - _Requirements: NFR-Security_

  - [x] 8.5 Implement request/response logging
    - Add logging filter for all requests
    - Log correlation ID, method, path, status, duration
    - Exclude sensitive fields (passwords, tokens) from logs
    - _Requirements: NFR-Observability_

  - [ ]* 8.6 Write unit tests for API Gateway
    - Test JWT validation and rejection of invalid tokens
    - Test rate limiting enforcement
    - Test role-based access control
    - Test routing to correct backend services


- [x] 9. Implement Redis and Kafka configuration
  - [x] 9.1 Configure Redis connection and Redisson client
    - Set up Redis connection properties in application.yml
    - Configure Redisson client with connection pooling
    - Set lock watchdog timeout to 30 seconds
    - Configure cache manager with TTL settings
    - _Requirements: FR-BK-03, NFR-Scalability_

  - [x] 9.2 Configure Kafka topics and producers
    - Create Kafka topic configurations: booking.confirmed, booking.cancelled, payment.success, payment.failed
    - Set partitions (6 for high-volume, 3 for low-volume topics)
    - Set replication factor to 3, min in-sync replicas to 2
    - Configure producer properties: acks=all, retries, idempotence
    - _Requirements: FR-BK-09, FR-PAY-03, FR-PAY-04_

  - [x] 9.3 Configure Kafka consumers with error handling
    - Set up consumer groups for each service
    - Configure consumer properties: auto-offset-reset, max-poll-records
    - Implement error handlers with retry logic (max 3 attempts)
    - Add dead letter queue for failed messages
    - _Requirements: FR-BK-09, FR-PAY-03, FR-PAY-04_

  - [x] 9.4 Implement cache strategies
    - Configure cache-aside pattern for show availability (60s TTL)
    - Configure local cache (Caffeine) for theatre metadata (10s TTL)
    - Implement cache invalidation on booking confirmation
    - _Requirements: NFR-Latency, NFR-Scalability_

- [ ] 10. Implement security features
  - [ ] 10.1 Configure HTTPS and TLS
    - Generate or import SSL certificates
    - Configure TLS 1.3 for all services
    - Set up certificate rotation strategy
    - _Requirements: NFR-Security_

  - [ ] 10.2 Implement input validation
    - Add Bean Validation (JSR-380) annotations to all DTOs
    - Create custom validators for email, password strength, seat numbers
    - Configure validation error responses with detailed messages
    - _Requirements: NFR-Security_

  - [ ] 10.3 Configure AWS Secrets Manager integration
    - Set up Secrets Manager for database credentials
    - Store JWT signing keys in Secrets Manager
    - Configure Redis and Kafka credentials
    - Update services to fetch secrets at startup
    - _Requirements: NFR-Security_

  - [ ] 10.4 Implement CORS configuration
    - Configure allowed origins, methods, headers
    - Set up preflight request handling
    - Configure credentials support
    - _Requirements: NFR-Security_

  - [ ]* 10.5 Write security tests
    - Test SQL injection prevention
    - Test XSS prevention in API responses
    - Test authentication bypass attempts
    - Test authorization bypass attempts

- [ ] 11. Checkpoint - Integration testing
  - Run end-to-end booking flow test (hold → payment → confirmation)
  - Test concurrent booking attempts on same seat
  - Verify Kafka event publishing and consumption
  - Test offer calculation with multiple eligible discounts
  - Ensure all tests pass, ask the user if questions arise

- [ ] 12. Implement monitoring and observability
  - [ ] 12.1 Configure Spring Boot Actuator
    - Enable health, metrics, info endpoints
    - Configure custom health indicators for database, Redis, Kafka
    - Set up liveness and readiness probes
    - _Requirements: NFR-Observability_

  - [ ] 12.2 Configure Micrometer metrics
    - Set up Micrometer with CloudWatch backend
    - Configure custom metrics: bookings per minute, seat hold success rate, payment success rate
    - Add JVM metrics: heap, GC, threads
    - Configure request/response metrics with percentiles (p50, p95, p99)
    - _Requirements: NFR-Observability_

  - [ ] 12.3 Configure AWS X-Ray distributed tracing
    - Add X-Ray SDK dependencies to all services
    - Configure trace ID propagation via HTTP headers
    - Set up sampling rules (100% for errors, 10% for success)
    - Create X-Ray segments for database and Redis calls
    - _Requirements: NFR-Observability_

  - [ ] 12.4 Configure CloudWatch Logs
    - Set up CloudWatch Logs agent for ECS tasks
    - Configure structured JSON logging with correlation IDs
    - Set log retention to 30 days
    - Create log groups per service
    - _Requirements: NFR-Observability_

  - [ ] 12.5 Create CloudWatch dashboards
    - Create service-level dashboards with key metrics
    - Create aggregated platform health dashboard
    - Add widgets for error rates, latency, throughput
    - Configure auto-refresh intervals
    - _Requirements: NFR-Observability_

  - [ ] 12.6 Configure alerting
    - Set up critical alerts: service health failures, database connection exhaustion, Kafka consumer lag > 1000
    - Set up warning alerts: high latency (p99 > threshold), cache miss rate > 30%
    - Configure PagerDuty integration for critical alerts
    - Configure Slack notifications for warning alerts
    - _Requirements: NFR-Observability_

- [ ] 13. Implement AWS ECS Fargate deployment configuration
  - [ ] 13.1 Create VPC and networking infrastructure
    - Create VPC with CIDR 10.0.0.0/16 across 3 AZs
    - Create public subnets for ALB and NAT Gateway
    - Create private subnets for ECS tasks
    - Create data subnets for RDS and Redis
    - Configure route tables and internet gateway
    - _Requirements: NFR-Availability_

  - [ ] 13.2 Create security groups
    - Create security group for ALB (allow 80, 443 from internet)
    - Create security group for ECS tasks (allow 8080-8085 from ALB)
    - Create security group for RDS (allow 5432 from ECS)
    - Create security group for Redis (allow 6379 from ECS)
    - Create security group for Kafka (allow 9092 from ECS)
    - _Requirements: NFR-Security_

  - [ ] 13.3 Set up RDS PostgreSQL Multi-AZ
    - Create RDS instance: db.r6g.xlarge, PostgreSQL 15
    - Enable Multi-AZ deployment for high availability
    - Configure automated backups (7-day retention)
    - Create read replicas for read-heavy queries
    - Configure connection pooling with HikariCP
    - _Requirements: NFR-Availability, NFR-Scalability_

  - [ ] 13.4 Set up ElastiCache Redis cluster
    - Create Redis cluster: cache.r6g.large, cluster mode enabled
    - Configure 3 shards with 2 replicas per shard
    - Enable automatic failover
    - Configure snapshot retention (5 days)
    - _Requirements: NFR-Availability, NFR-Scalability_

  - [ ] 13.5 Set up Amazon MSK Kafka cluster
    - Create MSK cluster with 3 brokers (kafka.m5.large)
    - Deploy brokers across 3 AZs
    - Configure 100 GB EBS storage per broker
    - Enable encryption in transit and at rest
    - _Requirements: NFR-Availability, NFR-Scalability_

  - [ ] 13.6 Create Application Load Balancer
    - Create internet-facing ALB in public subnets
    - Configure HTTP (80) listener with redirect to HTTPS
    - Configure HTTPS (443) listener with ACM certificate
    - Create target groups for each service
    - Configure health checks: /actuator/health (30s interval)
    - _Requirements: NFR-Availability_

  - [ ] 13.7 Create ECS cluster and task definitions
    - Create ECS Fargate cluster: movie-booking-cluster
    - Create task definitions for all 6 services with resource allocations
    - Configure environment variables from Secrets Manager
    - Set up CloudWatch Logs integration
    - Configure health check grace period (60 seconds)
    - _Requirements: NFR-Scalability_

  - [ ] 13.8 Configure ECS services with auto-scaling
    - Create ECS services for all 6 microservices
    - Set desired count: 2-3 tasks per service
    - Configure auto-scaling policies: target CPU 70%, memory 80%
    - Set min/max task counts per service
    - Configure scale-out cooldown (60s) and scale-in cooldown (300s)
    - _Requirements: NFR-Scalability_

  - [ ] 13.9 Set up AWS Cloud Map for service discovery
    - Create Cloud Map namespace: movie-booking.local
    - Register all services with Cloud Map
    - Configure DNS-based service discovery
    - Update service configurations to use service discovery
    - _Requirements: NFR-Scalability_

  - [ ] 13.10 Create Dockerfile for each service
    - Create multi-stage Dockerfile with Maven build
    - Use Eclipse Temurin JRE 17 as base image
    - Configure non-root user for security
    - Optimize layer caching for faster builds
    - _Requirements: All services_

  - [ ] 13.11 Set up Amazon ECR repositories
    - Create ECR repositories for all 6 services
    - Configure lifecycle policies (keep last 10 images)
    - Set up image scanning on push
    - Configure cross-region replication (optional)
    - _Requirements: All services_

- [ ] 14. Implement CI/CD pipeline
  - [ ] 14.1 Create GitHub Actions workflow for build and test
    - Set up workflow triggers: push to main, pull requests
    - Configure Maven build with dependency caching
    - Run unit tests and property-based tests
    - Run integration tests with Testcontainers
    - Generate test coverage reports
    - _Requirements: NFR-Observability_

  - [ ] 14.2 Create GitHub Actions workflow for Docker build and push
    - Build Docker images for all services
    - Tag images with commit SHA and latest
    - Push images to Amazon ECR
    - Run security scanning on images
    - _Requirements: All services_

  - [ ] 14.3 Create GitHub Actions workflow for ECS deployment
    - Update ECS task definitions with new image tags
    - Deploy to ECS with rolling update strategy
    - Wait for health checks to pass
    - Rollback on deployment failure
    - Send deployment notifications to Slack
    - _Requirements: NFR-Availability_

  - [ ] 14.4 Set up deployment environments
    - Configure development environment (local Docker Compose)
    - Configure staging environment (AWS ECS with smaller instances)
    - Configure production environment (AWS ECS with full configuration)
    - Set up environment-specific configuration files
    - _Requirements: All services_

- [ ] 15. Implement resilience patterns
  - [ ] 15.1 Configure Resilience4j circuit breaker
    - Add Resilience4j dependencies to all services
    - Configure circuit breaker: 50% failure threshold, 60s wait duration
    - Apply circuit breaker to all inter-service REST calls
    - Configure fallback strategies for read operations
    - _Requirements: NFR-Availability_

  - [ ] 15.2 Configure retry policies
    - Set max retry attempts to 3 with exponential backoff (1s, 2s, 4s)
    - Configure retryable errors: network errors, 503 errors
    - Configure non-retryable errors: 4xx errors (except 429)
    - _Requirements: NFR-Availability_

  - [ ] 15.3 Configure timeout settings
    - Set connection timeout to 5 seconds
    - Set read timeout to 10 seconds
    - Set seat lock operation timeout to 3 seconds
    - Configure timeout for inter-service calls
    - _Requirements: NFR-Latency_

  - [ ]* 15.4 Write resilience tests
    - Test circuit breaker activation and recovery
    - Test retry logic with transient failures
    - Test timeout handling
    - Test graceful degradation with cached data

- [ ] 16. Implement offer visibility endpoint
  - [ ] 16.1 Create offer query endpoint in Booking Service
    - Create GET /api/offers endpoint with city/theatre filters
    - Return list of available offers with conditions
    - Filter offers by location applicability
    - _Requirements: FR-SR-04_

  - [ ]* 16.2 Write property test for offer visibility
    - **Property 28: Offer visibility matches location**
    - **Validates: Requirements FR-SR-04**

- [ ] 17. Final integration and testing
  - [ ] 17.1 Run comprehensive integration test suite
    - Test complete booking flow: browse → hold → payment → confirmation → notification
    - Test cancellation flow: cancel → refund → notification
    - Test concurrent operations: multiple users booking same seats
    - Test bulk operations: bulk booking and bulk cancellation
    - _Requirements: All functional requirements_

  - [ ] 17.2 Run performance tests
    - Load test with 100k concurrent users simulation
    - Verify p99 latency: browse < 200ms, booking hold < 500ms
    - Test auto-scaling behavior under load
    - Verify database connection pool handling
    - _Requirements: NFR-Scalability, NFR-Latency_

  - [ ] 17.3 Run security tests
    - Run OWASP ZAP security scan
    - Test rate limiting enforcement
    - Test JWT token security (expiration, tampering)
    - Verify RBAC enforcement across all endpoints
    - _Requirements: NFR-Security_

  - [ ] 17.4 Verify all property-based tests pass
    - Run all 28 property tests with 100 iterations each
    - Verify no failing examples
    - Check test coverage for all requirements
    - _Requirements: All functional requirements_

- [ ] 18. Final checkpoint - Production readiness
  - Verify all services deployed to AWS ECS Fargate
  - Confirm all monitoring dashboards and alerts configured
  - Validate disaster recovery procedures
  - Ensure all documentation is complete
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Property-based tests validate universal correctness properties across all inputs
- Unit tests validate specific examples, edge cases, and error conditions
- Integration tests verify end-to-end flows and inter-service communication
- All 28 properties from the design document are covered by property-based tests
- Checkpoints ensure incremental validation and user feedback opportunities
- Services can be implemented in parallel after project setup is complete
- Redis and Kafka configuration should be completed before implementing dependent services
- AWS deployment can be done incrementally (start with single service, then scale)

