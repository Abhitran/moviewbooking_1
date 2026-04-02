package com.xyz.booking.dto;

import com.xyz.booking.entity.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID bookingId;
    private UUID userId;
    private UUID showId;
    private List<String> seats;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String offerApplied;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
