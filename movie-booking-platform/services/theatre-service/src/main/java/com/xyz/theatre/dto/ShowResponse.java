package com.xyz.theatre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResponse {
    private UUID showId;
    private UUID screenId;
    private String movieName;
    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal basePrice;
    private String language;
    private String genre;
    private Long availableSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
