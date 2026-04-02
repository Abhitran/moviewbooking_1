package com.xyz.theatre.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TheatreSearchResponse {
    private UUID theatreId;
    private String name;
    private String city;
    private String address;
    private List<ShowResponse> shows;
}
