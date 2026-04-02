package com.xyz.theatre.service;

import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.common.exception.UnauthorizedException;
import com.xyz.common.exception.ValidationException;
import com.xyz.theatre.dto.*;
import com.xyz.theatre.entity.*;
import com.xyz.theatre.repository.SeatRepository;
import com.xyz.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreService {
    
    private final TheatreRepository theatreRepository;
    private final SeatRepository seatRepository;
    
    @Transactional
    public TheatreResponse createTheatre(TheatreRequest request) {
        log.info("Creating theatre: {} in city: {}", request.getName(), request.getCity());
        
        // Create theatre entity
        Theatre theatre = Theatre.builder()
            .partnerId(request.getPartnerId())
            .name(request.getName())
            .city(request.getCity())
            .address(request.getAddress())
            .status(TheatreStatus.PENDING_APPROVAL)
            .build();
        
        // Add screens
        for (ScreenRequest screenReq : request.getScreens()) {
            Screen screen = Screen.builder()
                .name(screenReq.getName())
                .totalSeats(screenReq.getTotalSeats())
                .seatLayout(screenReq.getSeatLayout())
                .build();
            theatre.addScreen(screen);
        }
        
        Theatre savedTheatre = theatreRepository.save(theatre);
        log.info("Theatre created with ID: {}", savedTheatre.getId());
        
        return mapToTheatreResponse(savedTheatre);
    }
    
    @Transactional(readOnly = true)
    public TheatreResponse getTheatre(UUID theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        return mapToTheatreResponse(theatre);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public TheatreResponse approveTheatre(UUID theatreId, ApprovalRequest request) {
        log.info("Updating theatre {} status to: {}", theatreId, request.getStatus());
        
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        
        // Validate status transition
        if (theatre.getStatus() != TheatreStatus.PENDING_APPROVAL) {
            throw new ValidationException("Theatre is not in PENDING_APPROVAL status");
        }
        
        if (request.getStatus() == TheatreStatus.PENDING_APPROVAL) {
            throw new ValidationException("Cannot transition to PENDING_APPROVAL status");
        }
        
        theatre.setStatus(request.getStatus());
        Theatre savedTheatre = theatreRepository.save(theatre);
        
        log.info("Theatre {} status updated to: {}", theatreId, request.getStatus());
        return mapToTheatreResponse(savedTheatre);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "theatreSearch", key = "#city + '_' + #movieName + '_' + #date + '_' + #language + '_' + #genre")
    public List<TheatreSearchResponse> searchTheatres(
            String city, 
            String movieName, 
            LocalDate date,
            String language,
            String genre) {
        
        log.info("Searching theatres in {} for movie {} on {}", city, movieName, date);
        
        List<Theatre> theatres = theatreRepository.searchTheatres(city, movieName, date, language, genre);
        
        return theatres.stream()
            .map(this::mapToTheatreSearchResponse)
            .collect(Collectors.toList());
    }
    
    public void validateTheatreOwnership(UUID theatreId, UUID partnerId) {
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        
        if (!theatre.getPartnerId().equals(partnerId)) {
            throw new UnauthorizedException("You do not have permission to modify this theatre");
        }
    }
    
    private TheatreResponse mapToTheatreResponse(Theatre theatre) {
        List<ScreenResponse> screenResponses = theatre.getScreens().stream()
            .map(screen -> ScreenResponse.builder()
                .screenId(screen.getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .seatLayout(screen.getSeatLayout())
                .createdAt(screen.getCreatedAt())
                .build())
            .collect(Collectors.toList());
        
        return TheatreResponse.builder()
            .theatreId(theatre.getId())
            .partnerId(theatre.getPartnerId())
            .name(theatre.getName())
            .city(theatre.getCity())
            .address(theatre.getAddress())
            .status(theatre.getStatus())
            .screens(screenResponses)
            .createdAt(theatre.getCreatedAt())
            .updatedAt(theatre.getUpdatedAt())
            .build();
    }
    
    private TheatreSearchResponse mapToTheatreSearchResponse(Theatre theatre) {
        List<ShowResponse> showResponses = theatre.getScreens().stream()
            .flatMap(screen -> screen.getShows().stream())
            .map(show -> {
                long availableSeats = seatRepository.countByShowIdAndStatus(
                    show.getId(), SeatStatus.AVAILABLE);
                
                return ShowResponse.builder()
                    .showId(show.getId())
                    .screenId(show.getScreen().getId())
                    .movieName(show.getMovieName())
                    .showDate(show.getShowDate())
                    .showTime(show.getShowTime())
                    .basePrice(show.getBasePrice())
                    .language(show.getLanguage())
                    .genre(show.getGenre())
                    .availableSeats(availableSeats)
                    .createdAt(show.getCreatedAt())
                    .updatedAt(show.getUpdatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return TheatreSearchResponse.builder()
            .theatreId(theatre.getId())
            .name(theatre.getName())
            .city(theatre.getCity())
            .address(theatre.getAddress())
            .shows(showResponses)
            .build();
    }
}
