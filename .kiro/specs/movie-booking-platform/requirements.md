# XYZ Movie Booking Platform — Requirements

## 1. Overview

XYZ is an online movie ticket booking platform serving two client types:
- **B2B**: Theatre partners — onboard theatres, manage shows, seats, inventory
- **B2C**: End customers — browse movies, book/cancel tickets, apply offers

Tech stack: Java 17, Spring Boot 3.x, PostgreSQL, Redis, Kafka, AWS ECS Fargate

---

## 2. Actors & Roles

| Role | Description |
|---|---|
| SUPER_ADMIN | Platform admin, manages partners and global config |
| THEATRE_PARTNER | Manages their own theatres, screens, shows, seats |
| CUSTOMER | Browses, books, cancels tickets |
| GUEST | Browse only, no booking |

---

## 3. Functional Requirements

### 3.1 Auth Service
- FR-AUTH-01: User registration with email + password
- FR-AUTH-02: Login returns JWT access token + refresh token
- FR-AUTH-03: Role-based access control (SUPER_ADMIN, THEATRE_PARTNER, CUSTOMER)
- FR-AUTH-04: Token refresh endpoint
- FR-AUTH-05: Logout / token invalidation via Redis blacklist

### 3.2 Theatre Service (B2B)
- FR-TH-01: Theatre partner can register and onboard a theatre (name, city, address, screens)
- FR-TH-02: Partner can add/update/delete screens within a theatre
- FR-TH-03: Partner can create/update/delete shows (movie + screen + date + time + base price)
- FR-TH-04: Partner can allocate seat inventory per show (mark seats available/blocked)
- FR-TH-05: Partner can bulk update seat status for a show
- FR-TH-06: SUPER_ADMIN can approve/reject theatre onboarding

### 3.3 Booking Service (B2C)
- FR-BK-01: Customer can browse theatres showing a selected movie in a city on a given date
- FR-BK-02: Customer can view available seats for a show
- FR-BK-03: Customer can hold up to 10 seats for 10 minutes (Redis TTL lock)
- FR-BK-04: Customer confirms booking after payment
- FR-BK-05: Customer can cancel a booking (refund rules apply)
- FR-BK-06: Bulk booking — book multiple seats in one transaction
- FR-BK-07: Bulk cancellation — cancel multiple bookings at once
- FR-BK-08: Offers applied at checkout:
  - 50% discount on the 3rd ticket in a single booking
  - 20% discount on afternoon shows (12:00 PM – 5:00 PM)
- FR-BK-09: Booking confirmation sent via notification (email/SMS)

### 3.4 Payment Service
- FR-PAY-01: Initiate payment for a confirmed booking
- FR-PAY-02: Handle payment gateway webhook (success/failure)
- FR-PAY-03: On success — confirm booking, release seat hold, publish Kafka event
- FR-PAY-04: On failure — release seat hold, notify customer
- FR-PAY-05: Refund initiation on cancellation

### 3.5 Browse / Search
- FR-SR-01: Browse movies by city, language, genre, date
- FR-SR-02: Browse theatres currently running a selected movie in a city
- FR-SR-03: View show timings for a movie in a theatre on a date
- FR-SR-04: View offers available in selected cities/theatres

---

## 4. Non-Functional Requirements

### 4.1 Performance Requirements

| NFR-ID | Requirement | Target | Measurement |
|---|---|---|---|
| NFR-PERF-01 | Browse API Response Time | < 200ms (p99) | CloudWatch metrics |
| NFR-PERF-02 | Booking Hold Response Time | < 500ms (p99) | CloudWatch metrics |
| NFR-PERF-03 | Payment Initiation Response Time | < 1000ms (p99) | CloudWatch metrics |
| NFR-PERF-04 | Search API Response Time | < 300ms (p99) | CloudWatch metrics |
| NFR-PERF-05 | Database Query Time | < 100ms (p95) | RDS Performance Insights |
| NFR-PERF-06 | Redis Cache Hit Ratio | > 80% | Redis metrics |
| NFR-PERF-07 | Kafka Message Processing | < 2 seconds end-to-end | X-Ray tracing |

### 4.2 Scalability Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-SCALE-01 | Concurrent Users (Peak) | 100,000 users | ECS auto-scaling, load balancing |
| NFR-SCALE-02 | Concurrent Users (Normal) | 10,000 users | Baseline ECS task count |
| NFR-SCALE-03 | Bookings per Minute (Peak) | 5,000 bookings/min | Horizontal scaling, Redis locks |
| NFR-SCALE-04 | Bookings per Minute (Normal) | 500 bookings/min | Baseline capacity |
| NFR-SCALE-05 | API Requests per Second | 10,000 RPS | ALB + ECS auto-scaling |
| NFR-SCALE-06 | Database Connections | 200 per service instance | HikariCP connection pooling |
| NFR-SCALE-07 | Kafka Throughput | 10,000 messages/sec | MSK with 6 partitions |

### 4.3 Availability Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-AVAIL-01 | System Uptime | 99.99% (52 min downtime/year) | Multi-AZ deployment |
| NFR-AVAIL-02 | Database Availability | 99.95% | RDS Multi-AZ with auto-failover |
| NFR-AVAIL-03 | Cache Availability | 99.9% | ElastiCache cluster mode |
| NFR-AVAIL-04 | Service Recovery Time | < 2 minutes | ECS health checks, auto-restart |
| NFR-AVAIL-05 | Data Backup Frequency | Daily | RDS automated backups |
| NFR-AVAIL-06 | Backup Retention | 7 days | RDS configuration |
| NFR-AVAIL-07 | Disaster Recovery RTO | < 4 hours | Cross-region replication (future) |
| NFR-AVAIL-08 | Disaster Recovery RPO | < 1 hour | Point-in-time recovery |

### 4.4 Consistency Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-CONS-01 | Seat Booking Consistency | Strong consistency (no double booking) | Redis distributed locks + DB transactions |
| NFR-CONS-02 | Payment Consistency | Eventual consistency (< 5 sec) | Kafka events + idempotent processing |
| NFR-CONS-03 | Inventory Consistency | Strong consistency | Database ACID transactions |
| NFR-CONS-04 | Cache Consistency | Eventual consistency (< 60 sec) | TTL-based expiration + event invalidation |

### 4.5 Security Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-SEC-01 | Authentication | JWT with RS256 signing | Auth Service |
| NFR-SEC-02 | Authorization | Role-based access control (RBAC) | API Gateway + Service level |
| NFR-SEC-03 | Password Security | BCrypt (strength 12) | Auth Service |
| NFR-SEC-04 | Data Encryption (Transit) | TLS 1.3 | ALB + Service communication |
| NFR-SEC-05 | Data Encryption (Rest) | AES-256 | RDS encryption, EBS encryption |
| NFR-SEC-06 | Rate Limiting | 100 requests/min per user | API Gateway (Token Bucket) |
| NFR-SEC-07 | OWASP Top 10 Compliance | All threats mitigated | See Security section |
| NFR-SEC-08 | PCI-DSS Compliance | Payment data not stored | Stubbed gateway |
| NFR-SEC-09 | GDPR Compliance | User data protection | Data encryption, access controls |
| NFR-SEC-10 | Secret Management | Centralized secrets | AWS Secrets Manager |

### 4.6 Observability Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-OBS-01 | Centralized Logging | All services | CloudWatch Logs |
| NFR-OBS-02 | Log Retention | 30 days | CloudWatch configuration |
| NFR-OBS-03 | Distributed Tracing | End-to-end request tracing | AWS X-Ray |
| NFR-OBS-04 | Metrics Collection | Real-time metrics | CloudWatch + Micrometer |
| NFR-OBS-05 | Custom Business Metrics | Bookings/min, success rate | Custom CloudWatch metrics |
| NFR-OBS-06 | Alerting (Critical) | < 5 min detection | PagerDuty integration |
| NFR-OBS-07 | Alerting (Warning) | < 15 min detection | Slack notifications |
| NFR-OBS-08 | Dashboard Availability | Real-time dashboards | CloudWatch dashboards |

### 4.7 Maintainability Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-MAINT-01 | Code Coverage | > 80% | JUnit + Property-based tests |
| NFR-MAINT-02 | Deployment Frequency | Multiple times per day | CI/CD pipeline |
| NFR-MAINT-03 | Deployment Time | < 10 minutes | Rolling ECS deployment |
| NFR-MAINT-04 | Rollback Time | < 5 minutes | ECS task definition rollback |
| NFR-MAINT-05 | Documentation | Up-to-date API docs | OpenAPI/Swagger |

### 4.8 Compliance Requirements

| NFR-ID | Requirement | Target | Implementation |
|---|---|---|---|
| NFR-COMP-01 | PCI-DSS | Level 1 compliance | No payment data storage |
| NFR-COMP-02 | GDPR | User data protection | Encryption, access controls, audit logs |
| NFR-COMP-03 | Data Residency | India (future multi-region) | AWS Mumbai region |
| NFR-COMP-04 | Audit Trail | All critical operations logged | Event sourcing, CloudWatch |

---

## 5. User Capacity Planning

### 5.1 Target User Base

| User Type | Initial (Year 1) | Growth (Year 2) | Peak (Year 3) |
|---|---|---|---|
| Registered Customers | 1 Million | 5 Million | 10 Million |
| Active Monthly Users | 200,000 | 1 Million | 3 Million |
| Theatre Partners | 500 | 2,000 | 5,000 |
| Total Theatres | 1,000 | 4,000 | 10,000 |
| Total Screens | 5,000 | 20,000 | 50,000 |

### 5.2 Traffic Patterns

**Normal Load** (Weekdays, Non-Peak Hours):
- Concurrent users: 10,000
- Bookings per minute: 500
- API requests per second: 1,000 RPS
- Database queries per second: 2,000 QPS

**Peak Load** (Weekends, Blockbuster Releases, IPL Matches):
- Concurrent users: 100,000 (10x normal)
- Bookings per minute: 5,000 (10x normal)
- API requests per second: 10,000 RPS (10x normal)
- Database queries per second: 20,000 QPS (10x normal)

**Peak Events** (Major movie releases like Avengers, RRR):
- Duration: 2-4 hours
- Frequency: 5-10 times per year
- Expected traffic spike: 15-20x normal load
- Mitigation: Pre-scaling, queue-based booking

### 5.3 Data Volume Projections

| Data Type | Year 1 | Year 2 | Year 3 |
|---|---|---|---|
| Total Bookings | 10 Million | 50 Million | 150 Million |
| Total Users | 1 Million | 5 Million | 10 Million |
| Total Shows | 500,000 | 2 Million | 5 Million |
| Database Size | 50 GB | 250 GB | 750 GB |
| Redis Cache Size | 5 GB | 10 GB | 20 GB |
| Kafka Messages/Day | 1 Million | 5 Million | 15 Million |
| CloudWatch Logs/Day | 10 GB | 50 GB | 150 GB |

### 5.4 Geographic Distribution

**Phase 1** (Year 1): India - Top 10 Cities
- Mumbai, Delhi, Bangalore, Hyderabad, Chennai, Kolkata, Pune, Ahmedabad, Jaipur, Lucknow
- Single AWS region: ap-south-1 (Mumbai)

**Phase 2** (Year 2): India - Top 50 Cities
- Expand to tier-2 cities
- Consider multi-region within India

**Phase 3** (Year 3): International Expansion
- Southeast Asia, Middle East
- Multi-region deployment (Singapore, Dubai)

### 5.5 Capacity Planning by Service

#### 5.5.1 API Gateway
- Normal: 2 tasks (0.5 vCPU each) = 1 vCPU total
- Peak: 10 tasks = 5 vCPU total
- Handles: 10,000 RPS at peak

#### 5.5.2 Auth Service
- Normal: 2 tasks (0.5 vCPU each) = 1 vCPU total
- Peak: 8 tasks = 4 vCPU total
- Handles: 2,000 auth requests/sec at peak (heavily cached)

#### 5.5.3 Theatre Service
- Normal: 2 tasks (1 vCPU each) = 2 vCPU total
- Peak: 10 tasks = 10 vCPU total
- Handles: 3,000 search queries/sec at peak (read-heavy, cached)

#### 5.5.4 Booking Service (Most Critical)
- Normal: 3 tasks (2 vCPU each) = 6 vCPU total
- Peak: 20 tasks = 40 vCPU total
- Handles: 5,000 bookings/min = 83 bookings/sec at peak
- Bottleneck: Redis distributed locks (optimized for low latency)

#### 5.5.5 Payment Service
- Normal: 2 tasks (1 vCPU each) = 2 vCPU total
- Peak: 10 tasks = 10 vCPU total
- Handles: 5,000 payment initiations/min at peak

#### 5.5.6 Notification Service
- Normal: 2 tasks (0.5 vCPU each) = 1 vCPU total
- Peak: 5 tasks = 2.5 vCPU total
- Handles: 10,000 notifications/min at peak (async processing)

### 5.6 Database Capacity Planning

**RDS PostgreSQL**:
- Instance: db.r6g.xlarge (4 vCPU, 32 GB RAM)
- Storage: 500 GB GP3 SSD (expandable to 64 TB)
- IOPS: 12,000 baseline (burstable to 16,000)
- Connections: 200 per service instance × 50 instances = 10,000 max connections
- Read replicas: 2 (for read-heavy queries)

**Scaling Triggers**:
- CPU > 70% for 5 minutes → Scale up instance class
- Storage > 80% → Increase storage
- Connections > 80% → Add read replicas

### 5.7 Redis Capacity Planning

**ElastiCache Redis**:
- Node type: cache.r6g.large (2 vCPU, 13.07 GB RAM)
- Cluster: 3 shards × 2 replicas = 6 nodes total
- Total memory: 78 GB (13 GB × 6 nodes)
- Throughput: 250,000 ops/sec per shard = 750,000 ops/sec total

**Data Distribution**:
- Seat locks: 5 GB (peak: 100,000 concurrent holds × 50 KB each)
- Session cache: 2 GB (100,000 active sessions × 20 KB each)
- Show availability cache: 1 GB
- Token blacklist: 500 MB
- Rate limiting counters: 500 MB

### 5.8 Kafka Capacity Planning

**Amazon MSK**:
- Brokers: 3 × kafka.m5.large (2 vCPU, 8 GB RAM each)
- Storage: 100 GB EBS per broker = 300 GB total
- Throughput: 100 MB/sec per broker = 300 MB/sec total
- Messages: 10,000 messages/sec at peak

**Topic Sizing**:
- booking.confirmed: 5,000 messages/min × 2 KB = 10 MB/min = 14 GB/day
- payment.success: 5,000 messages/min × 1 KB = 5 MB/min = 7 GB/day
- Total: ~30 GB/day (retained for 7 days = 210 GB)

### 5.9 Cost Projections

| Component | Normal Load ($/month) | Peak Load ($/month) | Annual Average ($/month) |
|---|---|---|---|
| ECS Fargate | $400 | $1,200 | $600 |
| RDS PostgreSQL | $650 | $650 | $650 |
| ElastiCache Redis | $400 | $400 | $400 |
| Amazon MSK | $600 | $600 | $600 |
| ALB | $25 | $25 | $25 |
| Data Transfer | $50 | $150 | $80 |
| CloudWatch | $25 | $50 | $35 |
| Secrets Manager | $4 | $4 | $4 |
| **Total** | **$2,154** | **$3,079** | **$2,394** |

**Cost Optimization Strategies**:
- Reserved instances for baseline capacity (30% savings)
- Spot instances for non-critical workloads (70% savings)
- S3 archival for old bookings (90% storage cost reduction)
- CloudFront caching for static content (bandwidth savings)

### 5.10 Scaling Thresholds

| Metric | Scale Out Trigger | Scale In Trigger | Cooldown |
|---|---|---|---|
| CPU Utilization | > 70% for 2 min | < 30% for 10 min | 60s out, 300s in |
| Memory Utilization | > 80% for 2 min | < 40% for 10 min | 60s out, 300s in |
| Request Queue Depth | > 100 | < 10 | 60s out, 300s in |
| Response Time (p99) | > 1000ms | < 200ms | 60s out, 300s in |

### 5.11 Capacity Testing Plan

**Load Testing Schedule**:
- Weekly: Normal load (10,000 concurrent users)
- Monthly: Peak load (100,000 concurrent users)
- Quarterly: Stress test (150,000 concurrent users)
- Pre-release: Spike test (0 → 100,000 users in 5 minutes)

**Testing Tools**:
- JMeter for API load testing
- Gatling for realistic user scenarios
- Locust for distributed load generation
- AWS Load Testing Solution

**Success Criteria**:
- All NFRs met under peak load
- No errors or timeouts
- Auto-scaling triggers correctly
- Database and cache performance within limits

---

## 6. Offer Rules (Formal)

| Offer | Condition | Discount |
|---|---|---|
| Third ticket discount | booking_item_count >= 3 | 50% off on 3rd ticket price |
| Afternoon show discount | show start_time between 12:00 and 17:00 | 20% off total |
| Offers are mutually exclusive | Higher discount wins | — |

---

## 7. Constraints

- No UI required — REST APIs only
- Payment gateway is stubbed (no real gateway integration needed for assignment)
- Notification service is async via Kafka
- All services communicate via REST (sync) or Kafka (async)
