package com.xyz.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentInitiateRequest {
    private UUID bookingId;
    private BigDecimal amount;
    private UUID userId;
}
