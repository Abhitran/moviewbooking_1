package com.xyz.theatre.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
<<<<<<< HEAD
import jakarta.validation.constraints.NotNull;
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
public class TheatreRequest {
    
    @NotNull(message = "Partner ID is required")
    private UUID partnerId;
    
    @NotBlank(message = "Theatre name is required")
    private String name;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "Address is required")
    private String address;
    
=======
import lombok.Data;

import java.util.List;

@Data
public class TheatreRequest {

    @NotBlank(message = "Theatre name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    @NotEmpty(message = "At least one screen is required")
    @Valid
    private List<ScreenRequest> screens;
}
