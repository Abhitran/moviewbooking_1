package com.xyz.theatre.service;

import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.theatre.dto.ScreenRequest;
import com.xyz.theatre.dto.ScreenResponse;
import com.xyz.theatre.entity.Screen;
import com.xyz.theatre.entity.Theatre;
import com.xyz.theatre.repository.ScreenRepository;
import com.xyz.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenService {
    
    private final ScreenRepository screenRepository;
    private final TheatreRepository theatreRepository;
    private final TheatreService theatreService;
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public ScreenResponse addScreen(UUID theatreId, UUID partnerId, ScreenRequest request) {
        log.info("Adding screen {} to theatre {}", request.getName(), theatreId);
        
        // Validate ownership
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with ID: " + theatreId));
        
        Screen screen = Screen.builder()
            .name(request.getName())
            .totalSeats(request.getTotalSeats())
            .seatLayout(request.getSeatLayout())
            .build();
        
        theatre.addScreen(screen);
        theatreRepository.save(theatre);
        
        log.info("Screen added with ID: {}", screen.getId());
        return mapToScreenResponse(screen);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public ScreenResponse updateScreen(
            UUID theatreId, 
            UUID screenId, 
            UUID partnerId, 
            ScreenRequest request) {
        
        log.info("Updating screen {} in theatre {}", screenId, theatreId);
        
        // Validate ownership
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        Screen screen = screenRepository.findByIdAndTheatreId(screenId, theatreId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Screen not found with ID: " + screenId + " in theatre: " + theatreId));
        
        screen.setName(request.getName());
        screen.setTotalSeats(request.getTotalSeats());
        screen.setSeatLayout(request.getSeatLayout());
        
        Screen savedScreen = screenRepository.save(screen);
        log.info("Screen {} updated successfully", screenId);
        
        return mapToScreenResponse(savedScreen);
    }
    
    @Transactional
    @CacheEvict(value = "theatreSearch", allEntries = true)
    public void deleteScreen(UUID theatreId, UUID screenId, UUID partnerId) {
        log.info("Deleting screen {} from theatre {}", screenId, theatreId);
        
        // Validate ownership
        theatreService.validateTheatreOwnership(theatreId, partnerId);
        
        Screen screen = screenRepository.findByIdAndTheatreId(screenId, theatreId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Screen not found with ID: " + screenId + " in theatre: " + theatreId));
        
        screenRepository.delete(screen);
        log.info("Screen {} deleted successfully", screenId);
    }
    
    private ScreenResponse mapToScreenResponse(Screen screen) {
        return ScreenResponse.builder()
            .screenId(screen.getId())
            .name(screen.getName())
            .totalSeats(screen.getTotalSeats())
            .seatLayout(screen.getSeatLayout())
            .createdAt(screen.getCreatedAt())
            .build();
    }
}
