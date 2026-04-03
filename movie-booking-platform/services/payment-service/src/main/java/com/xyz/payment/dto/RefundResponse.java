package com.xyz.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Task 6.11: Response DTO for refund initiation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private UUID refundId;
    private UUID paymentId;
    private BigDecimal amount;
    private String status;
}
