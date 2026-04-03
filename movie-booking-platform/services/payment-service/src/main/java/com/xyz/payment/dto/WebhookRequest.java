package com.xyz.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for payment gateway webhook requests
 * Task 6.6: Webhook handler implementation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    
    @NotNull(message = "Payment ID is required")
    private UUID paymentId;
    
    @NotBlank(message = "Status is required")
    private String status; // "SUCCESS" or "FAILURE"
    
    @NotBlank(message = "Gateway transaction ID is required")
    private String gatewayTransactionId;
}
