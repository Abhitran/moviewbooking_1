package com.xyz.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HoldRequest {

    @NotNull(message = "showId is required")
    private UUID showId;

    @NotEmpty(message = "seatNumbers must not be empty")
    @Size(max = 10, message = "Cannot hold more than 10 seats at once")
    private List<String> seatNumbers;
}
