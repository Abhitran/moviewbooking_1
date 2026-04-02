package com.xyz.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class ShowResponse {
    private UUID showId;
    private UUID screenId;
    private String screenName;
    private String movieName;
    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal basePrice;
    private String language;
    private String genre;
    private long availableSeats;
}
