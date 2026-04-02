package com.xyz.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class RefundRequest {
    private UUID paymentId;
    private BigDecimal amount;
}
