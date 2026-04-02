# Theatre Service

Theatre Service manages theatre partners, screens, shows, and seat inventory for the XYZ Movie Booking Platform.

## Features

### Theatre Management
- **Theatre Registration**: Theatre partners can register their theatres with screens
- **Theatre Approval**: Super admins can approve/reject theatre registrations
- **Theatre Search**: Search theatres by city, movie, date, language, and genre

### Screen Management
- **Add Screen**: Add new screens to existing theatres
- **Update Screen**: Modify screen details and seat layouts
- **Delete Screen**: Remove screens from theatres

### Show Management
- **Create Show**: Schedule movie shows with date, time, and pricing
- **Update Show**: Modify show details
- **Delete Show**: Cancel scheduled shows
- **Duplicate Prevention**: Ensures no overlapping shows on the same screen

### Seat Inventory
- **Initialize Seats**: Create seat inventory for shows based on screen layout
- **Bulk Update**: Update multiple seat statuses (AVAILABLE/BLOCKED) atomically
- **Seat Tracking**: Track available seats per show

## API Endpoints

### Theatre Endpoints

```
POST   /api/theatres                          - Create theatre (THEATRE_PARTNER)
GET    /api/theatres/{theatreId}              - Get theatre details
PUT    /api/theatres/{theatreId}/approve      - Approve/reject theatre (SUPER_ADMIN)
GET    /api/theatres/search                   - Search theatres
```

### Screen Endpoints

```
POST   /api/theatres/{theatreId}/screens                    - Add screen
PUT    /api/theatres/{theatreId}/screens/{screenId}         - Update screen
DELETE /api/theatres/{theatreId}/screens/{screenId}         - Delete screen
```

### Show Endpoints

```
POST   /api/theatres/{theatreId}/shows        - Create show
PUT    /api/theatres/shows/{showId}           - Update show
DELETE /api/theatres/shows/{showId}           - Delete show
```

### Seat Endpoints

```
POST   /api/theatres/shows/{showId}/seats/initialize  - Initialize seats
PUT    /api/theatres/shows/{showId}/seats             - Bulk update seat status
```

## Database Schema

### Tables
- **theatres**: Theatre information and approval status
- **screens**: Screen details and seat layouts
- **shows**: Movie show schedules
- **seats**: Seat inventory per show

### Indexes
- `idx_theatres_city`: Fast city-based searches
- `idx_theatres_partner`: Partner's theatre lookup
- `idx_shows_movie`: Movie show searches
- `idx_seats_show`: Seat availability queries

## Caching Strategy

- **Theatre Search**: 60-second TTL Redis cache
- **Cache Invalidation**: Automatic on create/update/delete operations
- **Cache Key Format**: `city_movieName_date_language_genre`

## Configuration

### Environment Variables

```yaml
DB_HOST: PostgreSQL host (default: localhost)
DB_PORT: PostgreSQL port (default: 5432)
DB_NAME: Database name (default: moviebooking)
DB_USER: Database user (default: moviebooking)
DB_PASSWORD: Database password
REDIS_HOST: Redis host (default: localhost)
REDIS_PORT: Redis port (default: 6379)
```

### Application Properties

See `src/main/resources/application.yml` for full configuration.

## Running the Service

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Redis 7+

### Local Development

1. Start infrastructure:
```bash
docker-compose up -d postgres redis
```

2. Run the service:
```bash
./mvnw spring-boot:run -pl services/theatre-service
```

The service will start on port 8082.

## Architecture

### Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer**: Business logic separation
- **DTO Pattern**: Request/response data transfer
- **Cache-Aside**: Redis caching strategy

### Key Components
- **Entities**: JPA entities with relationships
- **Repositories**: Spring Data JPA repositories
- **Services**: Business logic layer
- **Controllers**: REST API endpoints
- **DTOs**: Request/response objects

## Security

- **Ownership Validation**: Partners can only modify their own theatres
- **Role-Based Access**: SUPER_ADMIN for approvals, THEATRE_PARTNER for management
- **Input Validation**: Jakarta Validation annotations on all DTOs

## Error Handling

- **ResourceNotFoundException**: 404 for missing resources
- **ValidationException**: 400 for validation errors
- **UnauthorizedException**: 403 for unauthorized access
- **Global Exception Handler**: Centralized error responses

## Future Enhancements

- [ ] Theatre partner dashboard
- [ ] Show analytics and reporting
- [ ] Dynamic pricing support
- [ ] Multi-language support
- [ ] Image upload for theatres/movies
