package com.xyz.theatre.dto;

import com.xyz.theatre.entity.TheatreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalRequest {

    @NotNull(message = "Status is required")
    private TheatreStatus status;
}
