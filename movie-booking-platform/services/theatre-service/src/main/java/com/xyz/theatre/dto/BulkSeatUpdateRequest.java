package com.xyz.theatre.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSeatUpdateRequest {
    
    @NotEmpty(message = "Seat updates cannot be empty")
    @Valid
    private List<SeatUpdateRequest> seatUpdates;
}
