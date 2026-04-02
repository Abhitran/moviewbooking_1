package com.xyz.theatre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheatreSearchResponse {
    private UUID theatreId;
    private String name;
    private String city;
    private String address;
    private List<ShowResponse> shows;
}
