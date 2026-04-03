# Task 6.4 Implementation: Payment Initiation Endpoint

## Overview
Implemented the payment initiation endpoint for the Payment Service as specified in task 6.4 of the movie-booking-platform spec.

## Requirements
- **Task**: 6.4 Implement payment initiation endpoint
- **Requirement**: FR-PAY-01 - Initiate payment for a confirmed booking
- **Endpoint**: POST /api/payments/initiate

## Implementation Details

### 1. DTOs Created

#### PaymentInitiateRequest.java
- **Location**: `src/main/java/com/xyz/payment/dto/PaymentInitiateRequest.java`
- **Fields**:
  - `bookingId` (UUID, required)
  - `amount` (BigDecimal, required, must be > 0)
  - `userId` (UUID, required)
- **Validation**: Uses Jakarta Bean Validation annotations

#### PaymentInitiateResponse.java
- **Location**: `src/main/java/com/xyz/payment/dto/PaymentInitiateResponse.java`
- **Fields**:
  - `paymentId` (UUID)
  - `gatewayUrl` (String)
  - `status` (String) - Returns "PENDING"

### 2. Service Layer

#### PaymentService.java
- **Location**: `src/main/java/com/xyz/payment/service/PaymentService.java`
- **Method**: `initiatePayment(PaymentInitiateRequest request)`
- **Logic**:
  1. Creates a Payment entity with PENDING status
  2. Saves the payment record to the database
  3. Generates a stubbed gateway URL
  4. Returns PaymentInitiateResponse with paymentId, gatewayUrl, and status

**Stubbed Gateway URL Format**:
```
https://stubbed-payment-gateway.example.com/pay?paymentId={paymentId}
```

### 3. Controller Layer

#### PaymentController.java
- **Location**: `src/main/java/com/xyz/payment/controller/PaymentController.java`
- **Endpoint**: `POST /api/payments/initiate`
- **Request Body**: PaymentInitiateRequest (JSON)
- **Response**: ApiResponse<PaymentInitiateResponse>
- **HTTP Status**: 200 OK on success

**Example Request**:
```json
{
  "bookingId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 500.00,
  "userId": "123e4567-e89b-12d3-a456-426614174001"
}
```

**Example Response**:
```json
{
  "success": true,
  "message": "Payment initiated successfully",
  "data": {
    "paymentId": "123e4567-e89b-12d3-a456-426614174002",
    "gatewayUrl": "https://stubbed-payment-gateway.example.com/pay?paymentId=123e4567-e89b-12d3-a456-426614174002",
    "status": "PENDING"
  }
}
```

### 4. Tests Created

#### PaymentServiceTest.java
- **Location**: `src/test/java/com/xyz/payment/service/PaymentServiceTest.java`
- **Test Cases**:
  1. `initiatePayment_shouldCreatePaymentWithPendingStatus` - Verifies payment is created with PENDING status
  2. `initiatePayment_shouldGenerateStubbedGatewayUrl` - Verifies gateway URL format
  3. `initiatePayment_shouldReturnCorrectResponseStructure` - Verifies response structure
  4. `initiatePayment_shouldHandleDifferentAmounts` - Tests with various amounts

#### PaymentControllerTest.java
- **Location**: `src/test/java/com/xyz/payment/controller/PaymentControllerTest.java`
- **Test Cases**:
  1. `initiatePayment_shouldReturnSuccessResponse` - Tests successful payment initiation
  2. `initiatePayment_shouldValidateRequiredFields` - Tests validation for missing fields
  3. `initiatePayment_shouldValidateAmountIsPositive` - Tests amount validation
  4. `initiatePayment_shouldAcceptValidRequest` - Tests valid request handling

## Database Schema
Uses existing Payment entity with the following fields:
- `payment_id` (UUID, primary key)
- `booking_id` (UUID, not null)
- `user_id` (UUID, not null)
- `amount` (DECIMAL, not null)
- `status` (VARCHAR, default: PENDING)
- `gateway_transaction_id` (VARCHAR, nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

## Integration with Booking Service
The Booking Service calls this endpoint via PaymentServiceClient:
```java
PaymentInitiateResponse response = paymentServiceClient.initiatePayment(paymentRequest);
```

## Design Patterns Used
1. **DTO Pattern**: Separate request/response objects for API contract
2. **Service Layer Pattern**: Business logic separated from controller
3. **Repository Pattern**: Data access abstraction via PaymentRepository
4. **Builder Pattern**: Used in DTOs and entities for clean object construction

## Compliance with Spec
✅ Creates payment record with PENDING status  
✅ Generates stubbed gateway URL  
✅ Returns paymentId and gatewayUrl  
✅ Follows REST API conventions  
✅ Uses ApiResponse wrapper for consistent responses  
✅ Implements proper validation  
✅ Includes comprehensive unit and integration tests  

## Next Steps
- Task 6.6: Implement payment gateway webhook handler
- Task 6.8: Implement payment success event publishing
- Task 6.9: Implement payment failure event publishing
