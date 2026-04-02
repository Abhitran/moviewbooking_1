package com.xyz.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HoldResponse {
    private String holdId;
    private LocalDateTime expiresAt;
    private List<String> seats;
}
