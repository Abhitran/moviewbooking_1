package com.xyz.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SeatAvailabilityResponse {
    private List<String> available;
    private List<String> held;
    private List<String> booked;
}
