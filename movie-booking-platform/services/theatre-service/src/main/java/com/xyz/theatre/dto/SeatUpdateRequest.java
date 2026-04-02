package com.xyz.theatre.dto;

import com.xyz.theatre.entity.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Status is required")
    private SeatStatus status;
}
