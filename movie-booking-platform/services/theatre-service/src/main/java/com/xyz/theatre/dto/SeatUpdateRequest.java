package com.xyz.theatre.dto;

import com.xyz.theatre.entity.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatUpdateRequest {
    
    @NotBlank(message = "Seat number is required")
    private String seatNumber;
    
=======
import lombok.Data;

@Data
public class SeatUpdateRequest {

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    @NotNull(message = "Status is required")
    private SeatStatus status;
}
