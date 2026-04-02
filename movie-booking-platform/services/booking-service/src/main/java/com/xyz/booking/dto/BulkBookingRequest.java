package com.xyz.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkBookingRequest {

    @NotEmpty(message = "bookings must not be empty")
    @Valid
    private List<HoldRequest> bookings;
}
