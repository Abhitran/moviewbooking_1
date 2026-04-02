package com.xyz.theatre.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
=======
import lombok.Data;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.util.List;

@Data
<<<<<<< HEAD
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkSeatUpdateRequest {
    
=======
public class BulkSeatUpdateRequest {

>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    @NotEmpty(message = "Seat updates cannot be empty")
    @Valid
    private List<SeatUpdateRequest> seatUpdates;
}
