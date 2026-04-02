# Movie Booking Platform - Repository Structure

## Mono-repo Structure (Recommended for Assignment)

```
movie-booking-platform/
│
├── .github/
│   └── workflows/
│       ├── build-all.yml              # Build all services
│       ├── auth-service-deploy.yml    # Deploy auth service
│       ├── theatre-service-deploy.yml
│       ├── booking-service-deploy.yml
│       ├── payment-service-deploy.yml
│       └── notification-service-deploy.yml
│
├── services/
│   ├── api-gateway/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/xyz/gateway/
│   │   │   │   │   ├── config/
│   │   │   │   │   ├── filter/
│   │   │   │   │   └── GatewayApplication.java
│   │   │   │   └── resources/
│   │   │   │       └── application.yml
│   │   │   └── test/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── auth-service/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/xyz/auth/
│   │   │   │   │   ├── controller/
│   │   │   │   │   ├── service/
│   │   │   │   │   ├── repository/
│   │   │   │   │   ├── entity/
│   │   │   │   │   ├── dto/
│   │   │   │   │   ├── config/
│   │   │   │   │   └── AuthServiceApplication.java
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       └── db/migration/
│   │   │   │           └── V1__create_users_table.sql
│   │   │   └── test/
│   │   ├── Dockerfile
│   │   └── pom.xml
│   │
│   ├── theatre-service/
│   ├── booking-service/
│   ├── payment-service/
│   └── notification-service/
│
├── common/
│   ├── domain-events/
│   │   ├── src/main/java/com/xyz/events/
│   │   │   ├── BookingConfirmedEvent.java
│   │   │   ├── PaymentSuccessEvent.java
│   │   │   └── ...
│   │   └── pom.xml
│   │
│   ├── shared-dtos/
│   │   ├── src/main/java/com/xyz/dto/
│   │   │   ├── ErrorResponse.java
│   │   │   ├── ApiResponse.java
│   │   │   └── ...
│   │   └── pom.xml
│   │
│   └── shared-utils/
│       ├── src/main/java/com/xyz/utils/
│       │   ├── CorrelationIdUtil.java
│       │   ├── DateTimeUtil.java
│       │   └── ...
│       └── pom.xml
│
├── infrastructure/
│   ├── docker/
│   │   └── docker-compose.yml         # Local dev environment
│   │
│   ├── terraform/
│   │   ├── main.tf
│   │   ├── vpc.tf
│   │   ├── ecs.tf
│   │   ├── rds.tf
│   │   └── variables.tf
│   │
│   └── k8s/                           # Optional: Kubernetes manifests
│       ├── api-gateway-deployment.yaml
│       ├── auth-service-deployment.yaml
│       └── ...
│
├── docs/
│   ├── api/
│   │   ├── auth-service-api.md
│   │   ├── theatre-service-api.md
│   │   └── ...
│   ├── architecture/
│   │   ├── hld.md
│   │   └── lld.md
│   └── deployment/
│       └── aws-deployment-guide.md
│
├── scripts/
│   ├── build-all.sh
│   ├── run-local.sh
│   └── deploy-to-aws.sh
│
├── .gitignore
├── pom.xml                            # Parent POM
├── README.md
└── ARCHITECTURE_SUMMARY.md
```

---

## Parent POM Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.xyz</groupId>
    <artifactId>movie-booking-platform</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <!-- Services -->
        <module>services/api-gateway</module>
        <module>services/auth-service</module>
        <module>services/theatre-service</module>
        <module>services/booking-service</module>
        <module>services/payment-service</module>
        <module>services/notification-service</module>
        
        <!-- Common modules -->
        <module>common/domain-events</module>
        <module>common/shared-dtos</module>
        <module>common/shared-utils</module>
    </modules>
    
    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencyManagement>
        <!-- Spring Boot BOM -->
        <!-- Spring Cloud BOM -->
        <!-- Common dependencies -->
    </dependencyManagement>
</project>
```

---

## Service-Level POM Example (auth-service)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.xyz</groupId>
        <artifactId>movie-booking-platform</artifactId>
        <version>1.0.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    
    <artifactId>auth-service</artifactId>
    <packaging>jar</packaging>
    
    <dependencies>
        <!-- Spring Boot starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Internal dependencies -->
        <dependency>
            <groupId>com.xyz</groupId>
            <artifactId>shared-dtos</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
</project>
```

---

## Docker Compose for Local Development

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: booking_platform
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7
    ports:
      - "6379:6379"
  
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
  
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
  
  # Services (built from local code)
  api-gateway:
    build: ../services/api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - auth-service
  
  auth-service:
    build: ../services/auth-service
    ports:
      - "8081:8081"
    depends_on:
      - postgres
      - redis
  
  # ... other services
```

---

## Build and Run Commands

### Build All Services
```bash
# From root directory
mvn clean install

# Build specific service
cd services/auth-service
mvn clean package
```

### Run Locally with Docker Compose
```bash
# Start infrastructure (DB, Redis, Kafka)
cd infrastructure/docker
docker-compose up -d postgres redis kafka

# Run services from IDE or command line
cd services/auth-service
mvn spring-boot:run
```

### Build Docker Images
```bash
# Build all service images
./scripts/build-all.sh

# Or individually
cd services/auth-service
docker build -t xyz/auth-service:latest .
```

---

## CI/CD Pipeline (GitHub Actions)

### Build All Services
```yaml
name: Build All Services

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean install
      
      - name: Run tests
        run: mvn test
```

### Deploy Individual Service
```yaml
name: Deploy Auth Service

on:
  push:
    branches: [main]
    paths:
      - 'services/auth-service/**'
      - 'common/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Docker image
        run: |
          cd services/auth-service
          docker build -t ${{ secrets.ECR_REGISTRY }}/auth-service:${{ github.sha }} .
      
      - name: Push to ECR
        run: docker push ${{ secrets.ECR_REGISTRY }}/auth-service:${{ github.sha }}
      
      - name: Deploy to ECS
        run: |
          aws ecs update-service \
            --cluster movie-booking-cluster \
            --service auth-service \
            --force-new-deployment
```

---

## Alternative: Multi-repo Structure (Production)

If you want to demonstrate multi-repo knowledge:

```
Organization: xyz-booking-platform

├── api-gateway/                    (Separate repo)
├── auth-service/                   (Separate repo)
├── theatre-service/                (Separate repo)
├── booking-service/                (Separate repo)
├── payment-service/                (Separate repo)
├── notification-service/           (Separate repo)
├── shared-libraries/               (Separate repo)
│   └── Published to Maven Central or private registry
└── infrastructure/                 (Separate repo)
    └── Terraform, K8s manifests
```

**Shared Libraries as Maven Artifacts:**
```xml
<dependency>
    <groupId>com.xyz.shared</groupId>
    <artifactId>domain-events</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Recommendation

For your assignment, use the **mono-repo approach**. It's:
- Easier to set up and demonstrate
- Standard for small-to-medium microservices projects
- Used by companies like Google, Facebook (monorepo at scale)
- Perfect for assignments and interviews

You can mention in your presentation:
> "I've used a mono-repo structure for easier demonstration, but in production, 
> we could split into separate repositories per service for true independence 
> and team ownership."

This shows you understand both approaches! 🎯
