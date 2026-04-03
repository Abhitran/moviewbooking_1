# API Gateway Service

The API Gateway is the single entry point for all client requests to the XYZ Movie Booking Platform. It handles routing, authentication, authorization, rate limiting, and request/response logging.

## Features

### 1. Routing Configuration
Routes requests to appropriate backend services:
- `/api/auth/**` → Auth Service (port 8081)
- `/api/theatres/**` → Theatre Service (port 8082)
- `/api/bookings/**` → Booking Service (port 8083)
- `/api/payments/**` → Payment Service (port 8084)

### 2. JWT Authentication
- Validates JWT tokens by calling Auth Service `/api/auth/validate` endpoint
- Extracts `userId` and `role` from validated tokens
- Adds `X-User-Id` and `X-User-Role` headers to downstream requests
- Public paths (no authentication required):
  - `/api/auth/register`
  - `/api/auth/login`

### 3. Role-Based Authorization
- Enforces role requirements at route level
- Returns 403 Forbidden for unauthorized access
- Supports multiple roles per route

### 4. Rate Limiting
- Token bucket algorithm using Redis
- Default: 100 requests per minute per user
- Uses user ID for authenticated requests, IP address for anonymous
- Returns 429 Too Many Requests when limit exceeded
- Adds rate limit headers to responses:
  - `X-RateLimit-Limit`: Maximum requests allowed
  - `X-RateLimit-Remaining`: Remaining requests in current window

### 5. Request/Response Logging
- Logs all incoming requests and outgoing responses
- Includes correlation ID, method, path, status code, and duration
- Excludes sensitive information (passwords, tokens) from logs

### 6. Correlation ID Propagation
- Generates or preserves correlation IDs for request tracing
- Adds `X-Correlation-ID` header to requests and responses
- Enables distributed tracing across microservices

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AUTH_SERVICE_HOST` | Auth service hostname | localhost |
| `THEATRE_SERVICE_HOST` | Theatre service hostname | localhost |
| `BOOKING_SERVICE_HOST` | Booking service hostname | localhost |
| `PAYMENT_SERVICE_HOST` | Payment service hostname | localhost |
| `REDIS_HOST` | Redis hostname | localhost |

### Application Properties

```yaml
server:
  port: 8080

auth:
  service:
    url: http://localhost:8081
  public-paths:
    - /api/auth/register
    - /api/auth/login

rate-limiter:
  requests-per-minute: 100
  redis-key-prefix: rate-limit
```

## Running the Service

### Prerequisites
- Java 17+
- Redis running on localhost:6379
- Auth Service running on localhost:8081

### Start the Gateway
```bash
cd movie-booking-platform/services/api-gateway
mvn spring-boot:run
```

The gateway will start on port 8080.

## Testing

### Run Unit Tests
```bash
mvn test
```

### Manual Testing

1. **Public endpoint (no auth required)**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

2. **Protected endpoint (auth required)**:
```bash
curl -X GET http://localhost:8080/api/theatres/search?city=Mumbai \
  -H "Authorization: Bearer <your-jwt-token>"
```

3. **Rate limiting test**:
```bash
# Make 101 requests rapidly to trigger rate limit
for i in {1..101}; do
  curl -X GET http://localhost:8080/api/auth/login
done
```

## Architecture

### Filter Chain Order
1. **CorrelationId** - Generate/preserve correlation ID
2. **RateLimiter** - Check rate limits (default filter)
3. **JwtAuthentication** - Validate JWT token (protected routes only)
4. **RoleBasedAuthorization** - Check user roles (if configured)
5. **RequestLogging** - Log request/response details

### Filter Implementations

- **JwtAuthenticationGatewayFilterFactory**: Validates JWT tokens with Auth Service
- **RoleBasedAuthorizationGatewayFilterFactory**: Enforces role-based access control
- **RateLimiterGatewayFilterFactory**: Implements token bucket rate limiting
- **RequestLoggingGatewayFilterFactory**: Logs request/response with correlation ID
- **CorrelationIdGatewayFilterFactory**: Manages correlation ID propagation

## Dependencies

- Spring Cloud Gateway
- Spring Boot Data Redis
- Spring Boot WebFlux
- JJWT (for JWT validation)
- Lombok

## Monitoring

### Logs
All requests are logged with:
- Correlation ID
- HTTP method and path
- User ID (if authenticated)
- Response status code
- Request duration in milliseconds

### Metrics
- Request count per route
- Response time percentiles
- Rate limit violations
- Authentication failures

## Security

- All passwords and tokens are excluded from logs
- JWT validation delegated to Auth Service
- Rate limiting prevents abuse
- HTTPS recommended for production (configure at load balancer level)

## Troubleshooting

### Common Issues

1. **401 Unauthorized**: Check if JWT token is valid and not expired
2. **403 Forbidden**: User role doesn't have permission for the route
3. **429 Too Many Requests**: Rate limit exceeded, wait for window to reset
4. **503 Service Unavailable**: Backend service is down or unreachable

### Debug Mode
Enable debug logging:
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    com.xyz.gateway: DEBUG
```
