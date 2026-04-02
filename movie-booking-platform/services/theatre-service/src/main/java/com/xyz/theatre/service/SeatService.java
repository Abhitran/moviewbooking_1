package com.xyz.theatre.service;

import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.common.exception.ValidationException;
import com.xyz.theatre.dto.BulkSeatUpdateRequest;
import com.xyz.theatre.dto.SeatUpdateRequest;
import com.xyz.theatre.entity.Seat;
import com.xyz.theatre.entity.Screen;
import com.xyz.theatre.entity.Show;
import com.xyz.theatre.repository.SeatRepository;
import com.xyz.theatre.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {
    
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final TheatreService theatreService;
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public void initializeSeats(UUID showId, UUID partnerId) {
        log.info("Initializing seats for show {}", showId);
        
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + showId));
        
        // Validate ownership
        UUID theatreId = show.getScreen().getTheatre().getId();
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        // Check if seats already exist
        long existingSeats = seatRepository.countByShowIdAndStatus(showId, null);
        if (existingSeats > 0) {
            throw new ValidationException("Seats already initialized for this show");
        }
        
        Screen screen = show.getScreen();
        List<Seat> seats = new ArrayList<>();
        
        // Extract seat numbers from seat layout
        Map<String, Object> seatLayout = screen.getSeatLayout();
        List<String> seatNumbers = extractSeatNumbers(seatLayout);
        
        for (String seatNumber : seatNumbers) {
            Seat seat = Seat.builder()
                .show(show)
                .seatNumber(seatNumber)
                .build();
            seats.add(seat);
        }
        
        seatRepository.saveAll(seats);
        log.info("Initialized {} seats for show {}", seats.size(), showId);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public void bulkUpdateSeatStatus(UUID showId, UUID partnerId, BulkSeatUpdateRequest request) {
        log.info("Bulk updating {} seats for show {}", request.getSeatUpdates().size(), showId);
        
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + showId));
        
        // Validate ownership
        UUID theatreId = show.getScreen().getTheatre().getId();
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        // Group updates by status
        Map<String, List<String>> updatesByStatus = request.getSeatUpdates().stream()
            .collect(Collectors.groupingBy(
                update -> update.getStatus().name(),
                Collectors.mapping(SeatUpdateRequest::getSeatNumber, Collectors.toList())
            ));
        
        // Execute updates in a single transaction
        for (Map.Entry<String, List<String>> entry : updatesByStatus.entrySet()) {
            List<String> seatNumbers = entry.getValue();
            
            // Validate all seats exist
            for (String seatNumber : seatNumbers) {
                if (!seatRepository.existsByShowIdAndSeatNumber(showId, seatNumber)) {
                    throw new ValidationException("Seat not found: " + seatNumber);
                }
            }
            
            // Update all seats with the same status in one query
            List<Seat> seats = seatNumbers.stream()
                .map(seatNumber -> seatRepository.findByShowIdAndSeatNumber(showId, seatNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatNumber)))
                .collect(Collectors.toList());
            
            seats.forEach(seat -> seat.setStatus(request.getSeatUpdates().stream()
                .filter(u -> u.getSeatNumber().equals(seat.getSeatNumber()))
                .findFirst()
                .orElseThrow()
                .getStatus()));
            
            seatRepository.saveAll(seats);
        }
        
        log.info("Bulk seat update completed for show {}", showId);
    }
    
    @SuppressWarnings("unchecked")
    private List<String> extractSeatNumbers(Map<String, Object> seatLayout) {
        List<String> seatNumbers = new ArrayList<>();
        
        // Assuming seat layout structure: { "rows": [ { "row": "A", "seats": ["A1", "A2", ...] }, ... ] }
        if (seatLayout.containsKey("rows")) {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) seatLayout.get("rows");
            for (Map<String, Object> row : rows) {
                if (row.containsKey("seats")) {
                    List<String> rowSeats = (List<String>) row.get("seats");
                    seatNumbers.addAll(rowSeats);
                }
            }
        }
        
        return seatNumbers;
    }
}
