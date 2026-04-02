package com.xyz.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkCancellationRequest {

    @NotEmpty(message = "bookingIds must not be empty")
    private List<UUID> bookingIds;
}
