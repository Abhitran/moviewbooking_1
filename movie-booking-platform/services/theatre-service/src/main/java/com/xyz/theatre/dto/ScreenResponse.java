package com.xyz.theatre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreenResponse {
    private UUID screenId;
    private String name;
    private Integer totalSeats;
    private Map<String, Object> seatLayout;
    private LocalDateTime createdAt;
}
