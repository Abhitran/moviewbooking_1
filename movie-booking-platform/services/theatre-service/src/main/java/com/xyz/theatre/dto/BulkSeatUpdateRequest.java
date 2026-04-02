package com.xyz.theatre.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkSeatUpdateRequest {

    @NotEmpty(message = "Seat updates cannot be empty")
    @Valid
    private List<SeatUpdateRequest> seatUpdates;
}
