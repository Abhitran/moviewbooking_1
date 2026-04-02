package com.xyz.theatre.dto;

import com.xyz.theatre.entity.TheatreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheatreResponse {
    private UUID theatreId;
    private UUID partnerId;
    private String name;
    private String city;
    private String address;
    private TheatreStatus status;
    private List<ScreenResponse> screens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
