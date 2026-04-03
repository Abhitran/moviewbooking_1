package com.xyz.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for payment details query.
 * Task 6.12: GET /api/payments/{paymentId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailsResponse {

    private UUID paymentId;
    
    private UUID bookingId;
    
    private UUID userId;
    
    private BigDecimal amount;
    
    private String status;
    
    private String gatewayTransactionId;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
