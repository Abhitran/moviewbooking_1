package com.xyz.booking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmBookingRequest {

    @NotBlank(message = "holdId is required")
    private String holdId;
}
