package com.xyz.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CancellationResponse {
    private UUID bookingId;
    private BigDecimal refundAmount;
    private String status;
}
