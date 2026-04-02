package com.xyz.theatre.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequest {
    
    @NotNull(message = "Screen ID is required")
    private UUID screenId;
    
    @NotBlank(message = "Movie name is required")
    private String movieName;
    
    @NotNull(message = "Show date is required")
    @FutureOrPresent(message = "Show date must be today or in the future")
    private LocalDate showDate;
    
    @NotNull(message = "Show time is required")
    private LocalTime showTime;
    
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    private BigDecimal basePrice;
    
    private String language;
    
    private String genre;
}
