package com.xyz.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ScreenResponse {
    private UUID screenId;
    private String name;
    private Integer totalSeats;
    private Map<String, Object> seatLayout;
}
