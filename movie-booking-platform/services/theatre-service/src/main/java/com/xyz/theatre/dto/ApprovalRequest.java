package com.xyz.theatre.dto;

import com.xyz.theatre.entity.TheatreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    
    @NotNull(message = "Status is required")
    private TheatreStatus status;
}
