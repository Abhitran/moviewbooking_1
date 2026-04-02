package com.xyz.theatre.dto;

import com.xyz.theatre.entity.SeatStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SeatResponse {
    private UUID seatId;
    private String seatNumber;
    private SeatStatus status;
}
