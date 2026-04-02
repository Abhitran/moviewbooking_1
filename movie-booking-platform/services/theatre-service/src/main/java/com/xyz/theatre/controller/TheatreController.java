package com.xyz.theatre.controller;

import com.xyz.common.dto.ApiResponse;
import com.xyz.theatre.dto.*;
<<<<<<< HEAD
import com.xyz.theatre.service.ScreenService;
import com.xyz.theatre.service.SeatService;
import com.xyz.theatre.service.ShowService;
=======
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
import com.xyz.theatre.service.TheatreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
=======
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

<<<<<<< HEAD
@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
@Slf4j
public class TheatreController {
    
    private final TheatreService theatreService;
    private final ScreenService screenService;
    private final ShowService showService;
    private final SeatService seatService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<TheatreResponse>> createTheatre(
            @Valid @RequestBody TheatreRequest request) {
        
        log.info("POST /api/theatres - Creating theatre: {}", request.getName());
        TheatreResponse response = theatreService.createTheatre(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Theatre created successfully"));
    }
    
    @GetMapping("/{theatreId}")
    public ResponseEntity<ApiResponse<TheatreResponse>> getTheatre(
            @PathVariable UUID theatreId) {
        
        log.info("GET /api/theatres/{} - Fetching theatre", theatreId);
        TheatreResponse response = theatreService.getTheatre(theatreId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{theatreId}/approve")
    public ResponseEntity<ApiResponse<TheatreResponse>> approveTheatre(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ApprovalRequest request,
            @RequestHeader("X-User-Role") String role) {
        
        log.info("PUT /api/theatres/{}/approve - Approving theatre", theatreId);
        
        // Role validation should be done by API Gateway, but we can add a check here
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Only SUPER_ADMIN can approve theatres"));
        }
        
        TheatreResponse response = theatreService.approveTheatre(theatreId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Theatre status updated"));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TheatreSearchResponse>>> searchTheatres(
            @RequestParam String city,
            @RequestParam String movieName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre) {
        
        log.info("GET /api/theatres/search - city: {}, movie: {}, date: {}", city, movieName, date);
        List<TheatreSearchResponse> response = theatreService.searchTheatres(
            city, movieName, date, language, genre);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    // Screen Management
    
    @PostMapping("/{theatreId}/screens")
    public ResponseEntity<ApiResponse<ScreenResponse>> addScreen(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ScreenRequest request,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("POST /api/theatres/{}/screens - Adding screen", theatreId);
        ScreenResponse response = screenService.addScreen(theatreId, partnerId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Screen added successfully"));
    }
    
    @PutMapping("/{theatreId}/screens/{screenId}")
=======
@Slf4j
@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreService theatreService;

    // ─── Theatre ────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<TheatreResponse>> createTheatre(
            @Valid @RequestBody TheatreRequest request,
            @AuthenticationPrincipal String userId) {
        log.debug("POST /api/theatres by partner {}", userId);
        TheatreResponse response = theatreService.createTheatre(request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Theatre created successfully", response));
    }

    @GetMapping("/{theatreId}")
    public ResponseEntity<ApiResponse<TheatreResponse>> getTheatre(
            @PathVariable UUID theatreId) {
        return ResponseEntity.ok(ApiResponse.success(theatreService.getTheatre(theatreId)));
    }

    @PutMapping("/{theatreId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TheatreResponse>> approveTheatre(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ApprovalRequest request) {
        log.debug("PUT /api/theatres/{}/approve status={}", theatreId, request.getStatus());
        TheatreResponse response = theatreService.approveTheatre(theatreId, request);
        return ResponseEntity.ok(ApiResponse.success("Theatre status updated", response));
    }

    // ─── Screen ─────────────────────────────────────────────────────────────────

    @PostMapping("/{theatreId}/screens")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<ScreenResponse>> addScreen(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ScreenRequest request,
            @AuthenticationPrincipal String userId) {
        ScreenResponse response = theatreService.addScreen(theatreId, request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Screen added successfully", response));
    }

    @PutMapping("/{theatreId}/screens/{screenId}")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    public ResponseEntity<ApiResponse<ScreenResponse>> updateScreen(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId,
            @Valid @RequestBody ScreenRequest request,
<<<<<<< HEAD
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("PUT /api/theatres/{}/screens/{} - Updating screen", theatreId, screenId);
        ScreenResponse response = screenService.updateScreen(theatreId, screenId, partnerId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Screen updated successfully"));
    }
    
    @DeleteMapping("/{theatreId}/screens/{screenId}")
    public ResponseEntity<ApiResponse<Void>> deleteScreen(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("DELETE /api/theatres/{}/screens/{} - Deleting screen", theatreId, screenId);
        screenService.deleteScreen(theatreId, screenId, partnerId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Screen deleted successfully"));
    }
    
    // Show Management
    
    @PostMapping("/{theatreId}/shows")
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ShowRequest request,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("POST /api/theatres/{}/shows - Creating show", theatreId);
        ShowResponse response = showService.createShow(theatreId, partnerId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Show created successfully"));
    }
    
    @PutMapping("/shows/{showId}")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShow(
            @PathVariable UUID showId,
            @Valid @RequestBody ShowRequest request,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("PUT /api/theatres/shows/{} - Updating show", showId);
        ShowResponse response = showService.updateShow(showId, partnerId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Show updated successfully"));
    }
    
    @DeleteMapping("/shows/{showId}")
    public ResponseEntity<ApiResponse<Void>> deleteShow(
            @PathVariable UUID showId,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("DELETE /api/theatres/shows/{} - Deleting show", showId);
        showService.deleteShow(showId, partnerId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Show deleted successfully"));
    }
    
    // Seat Management
    
    @PostMapping("/shows/{showId}/seats/initialize")
    public ResponseEntity<ApiResponse<Void>> initializeSeats(
            @PathVariable UUID showId,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("POST /api/theatres/shows/{}/seats/initialize - Initializing seats", showId);
        seatService.initializeSeats(showId, partnerId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(null, "Seats initialized successfully"));
    }
    
    @PutMapping("/shows/{showId}/seats")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateSeats(
            @PathVariable UUID showId,
            @Valid @RequestBody BulkSeatUpdateRequest request,
            @RequestHeader("X-User-Id") UUID partnerId) {
        
        log.info("PUT /api/theatres/shows/{}/seats - Bulk updating seats", showId);
        seatService.bulkUpdateSeatStatus(showId, partnerId, request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Seats updated successfully"));
=======
            @AuthenticationPrincipal String userId) {
        ScreenResponse response = theatreService.updateScreen(
            theatreId, screenId, request, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Screen updated", response));
    }

    @DeleteMapping("/{theatreId}/screens/{screenId}")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<Void>> deleteScreen(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId,
            @AuthenticationPrincipal String userId) {
        theatreService.deleteScreen(theatreId, screenId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Screen deleted", null));
    }

    // ─── Show ────────────────────────────────────────────────────────────────────

    @PostMapping("/{theatreId}/shows")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @PathVariable UUID theatreId,
            @Valid @RequestBody ShowRequest request,
            @AuthenticationPrincipal String userId) {
        ShowResponse response = theatreService.createShow(theatreId, request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Show created successfully", response));
    }

    @PutMapping("/shows/{showId}")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<ShowResponse>> updateShow(
            @PathVariable UUID showId,
            @Valid @RequestBody ShowRequest request,
            @AuthenticationPrincipal String userId) {
        ShowResponse response = theatreService.updateShow(showId, request, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Show updated", response));
    }

    @DeleteMapping("/shows/{showId}")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<Void>> deleteShow(
            @PathVariable UUID showId,
            @AuthenticationPrincipal String userId) {
        theatreService.deleteShow(showId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Show deleted", null));
    }

    // ─── Seat Inventory ──────────────────────────────────────────────────────────

    @PostMapping("/shows/{showId}/seats")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<List<SeatResponse>>> initializeSeats(
            @PathVariable UUID showId,
            @Valid @RequestBody SeatInitRequest request,
            @AuthenticationPrincipal String userId) {
        List<SeatResponse> response = theatreService.initializeSeats(
            showId, request, UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Seats initialized", response));
    }

    @PutMapping("/shows/{showId}/seats")
    @PreAuthorize("hasRole('THEATRE_PARTNER')")
    public ResponseEntity<ApiResponse<Integer>> bulkUpdateSeats(
            @PathVariable UUID showId,
            @Valid @RequestBody BulkSeatUpdateRequest request,
            @AuthenticationPrincipal String userId) {
        int updated = theatreService.bulkUpdateSeats(showId, request, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Seats updated", updated));
    }

    // ─── Search ──────────────────────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TheatreSearchResponse>>> searchTheatres(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String movieName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre) {
        List<TheatreSearchResponse> results = theatreService.searchTheatres(
            city, movieName, date, language, genre);
        return ResponseEntity.ok(ApiResponse.success(results));
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
    }
}
