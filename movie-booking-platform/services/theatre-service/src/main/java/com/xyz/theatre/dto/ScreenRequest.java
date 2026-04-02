package com.xyz.theatre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
=======
import lombok.Data;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3

import java.util.Map;

@Data
<<<<<<< HEAD
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenRequest {
    
    @NotBlank(message = "Screen name is required")
    private String name;
    
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
    
    @NotNull(message = "Seat layout is required")
=======
public class ScreenRequest {

    @NotBlank(message = "Screen name is required")
    private String name;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;

>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    private Map<String, Object> seatLayout;
}
