package com.xyz.theatre.controller;

import com.xyz.common.dto.ApiResponse;
import com.xyz.theatre.dto.*;
import com.xyz.theatre.service.TheatreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<ApiResponse<ScreenResponse>> updateScreen(
            @PathVariable UUID theatreId,
            @PathVariable UUID screenId,
            @Valid @RequestBody ScreenRequest request,
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
    }
}
