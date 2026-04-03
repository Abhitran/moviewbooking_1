# Notification Service

## Overview
The Notification Service is responsible for sending notifications to users via email and SMS. It consumes events from Kafka topics and processes them asynchronously.

## Port
- **8085**

## Dependencies
- Spring Boot 3.x
- Spring Kafka
- Domain Events (common module)
- Shared DTOs (common module)
- Shared Utils (common module)

## Kafka Topics Consumed

### 1. booking.confirmed
- **Event Type**: `BookingConfirmedEvent`
- **Action**: Sends booking confirmation via email and SMS
- **Fields**: bookingId, userId, showId, userEmail, totalAmount

### 2. booking.cancelled
- **Event Type**: `BookingConfirmedEvent` (reused)
- **Action**: Sends booking cancellation notification via email and SMS
- **Fields**: bookingId, userId, userEmail, totalAmount (refund)

### 3. payment.failed
- **Event Type**: `PaymentFailedEvent`
- **Action**: Sends payment failure notification via email and SMS
- **Fields**: paymentId, bookingId, reason

## Configuration

### Application Properties
```yaml
server:
  port: 8085

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service

notification:
  email:
    stub-mode: true  # Set to false for real email sending
  sms:
    stub-mode: true  # Set to false for real SMS sending
```

## Stub Implementation
Currently, both email and SMS notifications are stubbed and log to the console. This is suitable for development and testing.

To enable real notifications:
1. Set `notification.email.stub-mode=false` and `notification.sms.stub-mode=false`
2. Implement actual email/SMS gateway integration in the respective service classes

## API Endpoints

### Health Check
```
GET /api/notifications/health
```
Returns the health status of the notification service.

## Running the Service

### Prerequisites
- Kafka running on localhost:9092
- Java 17+

### Start the Service
```bash
mvn spring-boot:run
```

Or with Docker:
```bash
docker-compose up notification-service
```

## Architecture

```
Kafka Topics → NotificationEventConsumer → NotificationService
                                              ├── EmailNotificationService (stub)
                                              └── SmsNotificationService (stub)
```

## Package Structure
```
com.xyz.notification
├── config/              # Kafka consumer configuration
├── consumer/            # Kafka event consumers
├── controller/          # REST controllers (health check)
├── dto/                 # Data transfer objects (future use)
├── exception/           # Custom exceptions (future use)
└── service/             # Business logic
    ├── NotificationService.java
    ├── EmailNotificationService.java
    └── SmsNotificationService.java
```

## Future Enhancements
- Integration with real email providers (SendGrid, AWS SES)
- Integration with SMS gateways (Twilio, AWS SNS)
- Notification templates
- Retry mechanism for failed notifications
- Dead letter queue for unprocessable events
