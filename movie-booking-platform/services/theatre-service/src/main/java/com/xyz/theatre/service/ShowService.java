package com.xyz.theatre.service;

import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.common.exception.ValidationException;
import com.xyz.theatre.dto.ShowRequest;
import com.xyz.theatre.dto.ShowResponse;
import com.xyz.theatre.entity.Screen;
import com.xyz.theatre.entity.SeatStatus;
import com.xyz.theatre.entity.Show;
import com.xyz.theatre.repository.ScreenRepository;
import com.xyz.theatre.repository.SeatRepository;
import com.xyz.theatre.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {
    
    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final TheatreService theatreService;
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public ShowResponse createShow(UUID theatreId, UUID partnerId, ShowRequest request) {
        log.info("Creating show for movie {} on {} at {}", 
            request.getMovieName(), request.getShowDate(), request.getShowTime());
        
        // Validate ownership
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        Screen screen = screenRepository.findById(request.getScreenId())
            .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + request.getScreenId()));
        
        // Validate screen belongs to theatre
        if (!screen.getTheatre().getId().equals(theatreId)) {
            throw new ValidationException("Screen does not belong to this theatre");
        }
        
        // Check for duplicate show time
        showRepository.findByScreenIdAndShowDateAndShowTime(
            request.getScreenId(), request.getShowDate(), request.getShowTime())
            .ifPresent(existingShow -> {
                throw new ValidationException("A show already exists at this time slot");
            });
        
        Show show = Show.builder()
            .screen(screen)
            .movieName(request.getMovieName())
            .showDate(request.getShowDate())
            .showTime(request.getShowTime())
            .basePrice(request.getBasePrice())
            .language(request.getLanguage())
            .genre(request.getGenre())
            .build();
        
        Show savedShow = showRepository.save(show);
        log.info("Show created with ID: {}", savedShow.getId());
        
        return mapToShowResponse(savedShow);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public ShowResponse updateShow(UUID showId, UUID partnerId, ShowRequest request) {
        log.info("Updating show {}", showId);
        
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + showId));
        
        // Validate ownership
        UUID theatreId = show.getScreen().getTheatre().getId();
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        // If screen is being changed, validate it
        if (!show.getScreen().getId().equals(request.getScreenId())) {
            Screen newScreen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with ID: " + request.getScreenId()));
            
            if (!newScreen.getTheatre().getId().equals(theatreId)) {
                throw new ValidationException("Screen does not belong to this theatre");
            }
            
            show.setScreen(newScreen);
        }
        
        // Check for duplicate show time (excluding current show)
        showRepository.findByScreenIdAndShowDateAndShowTime(
            request.getScreenId(), request.getShowDate(), request.getShowTime())
            .ifPresent(existingShow -> {
                if (!existingShow.getId().equals(showId)) {
                    throw new ValidationException("A show already exists at this time slot");
                }
            });
        
        show.setMovieName(request.getMovieName());
        show.setShowDate(request.getShowDate());
        show.setShowTime(request.getShowTime());
        show.setBasePrice(request.getBasePrice());
        show.setLanguage(request.getLanguage());
        show.setGenre(request.getGenre());
        
        Show savedShow = showRepository.save(show);
        log.info("Show {} updated successfully", showId);
        
        return mapToShowResponse(savedShow);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public void deleteShow(UUID showId, UUID partnerId) {
        log.info("Deleting show {}", showId);
        
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new ResourceNotFoundException("Show not found with ID: " + showId));
        
        // Validate ownership
        UUID theatreId = show.getScreen().getTheatre().getId();
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        showRepository.delete(show);
        log.info("Show {} deleted successfully", showId);
    }
    
    private ShowResponse mapToShowResponse(Show show) {
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
    }
}
