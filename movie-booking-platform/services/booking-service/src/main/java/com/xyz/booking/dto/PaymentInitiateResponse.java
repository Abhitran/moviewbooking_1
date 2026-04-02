package com.xyz.booking.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentInitiateResponse {
    private UUID paymentId;
    private String gatewayUrl;
    private String status;
}
