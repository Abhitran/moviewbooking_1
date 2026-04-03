package com.xyz.booking.controller;

import com.xyz.booking.dto.*;
import com.xyz.booking.service.BookingService;
import com.xyz.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for booking operations.
 * Implements tasks 5.14, 5.18, 5.20, 5.22, 5.24
 */
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Task 5.14: POST /api/bookings/confirm
     * Confirms a booking after validating hold and calculating offers
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("Confirming booking for holdId: {} by user: {}", request.getHoldId(), userId);
        
        BookingResponse response = bookingService.confirmBooking(request.getHoldId(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Booking confirmed successfully"));
    }

    /**
     * Task 5.18: DELETE /api/bookings/{bookingId}
     * Cancels a confirmed booking and initiates refund
     */
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<CancellationResponse>> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);
        
        CancellationResponse response = bookingService.cancelBooking(bookingId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Booking cancelled successfully"));
    }

    /**
     * Task 5.20: POST /api/bookings/bulk
     * Creates multiple bookings in a single transaction
     */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> bulkBooking(
            @Valid @RequestBody BulkBookingRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("Processing bulk booking for {} requests by user: {}", 
                request.getBookings().size(), userId);
        
        List<BookingResponse> responses = bookingService.bulkBooking(request.getBookings(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses, 
                "Bulk booking completed successfully"));
    }

    /**
     * Task 5.22: DELETE /api/bookings/bulk
     * Cancels multiple bookings in a single transaction
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<ApiResponse<List<CancellationResponse>>> bulkCancellation(
            @Valid @RequestBody BulkCancellationRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        log.info("Processing bulk cancellation for {} bookings by user: {}", 
                request.getBookingIds().size(), userId);
        
        List<CancellationResponse> responses = bookingService.bulkCancellation(
                request.getBookingIds(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(responses, 
                "Bulk cancellation completed successfully"));
    }

    /**
     * Task 5.24: GET /api/bookings/user/{userId}
     * Returns all bookings for a user with pagination (page size: 20)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getUserBookings(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page) {
        
        log.info("Fetching bookings for user: {}, page: {}", userId, page);
        
        Page<BookingResponse> bookings = bookingService.getUserBookings(userId, page);
        
        return ResponseEntity.ok(ApiResponse.success(bookings, 
                "User bookings retrieved successfully"));
    }
}
