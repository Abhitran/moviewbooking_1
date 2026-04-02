package com.xyz.theatre.service;

<<<<<<< HEAD
import com.xyz.common.exception.ResourceNotFoundException;
import com.xyz.common.exception.UnauthorizedException;
import com.xyz.common.exception.ValidationException;
import com.xyz.theatre.dto.*;
import com.xyz.theatre.entity.*;
import com.xyz.theatre.repository.SeatRepository;
import com.xyz.theatre.repository.TheatreRepository;
=======
import com.xyz.theatre.dto.*;
import com.xyz.theatre.entity.*;
import com.xyz.theatre.exception.TheatreServiceException;
import com.xyz.theatre.repository.*;
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
<<<<<<< HEAD
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
=======
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;

    // ─── Theatre ────────────────────────────────────────────────────────────────

    @Transactional
    public TheatreResponse createTheatre(TheatreRequest request, UUID partnerId) {
        Theatre theatre = Theatre.builder()
            .partnerId(partnerId)
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
            .name(request.getName())
            .city(request.getCity())
            .address(request.getAddress())
            .status(TheatreStatus.PENDING_APPROVAL)
            .build();
<<<<<<< HEAD
        
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
=======

        List<Screen> screens = request.getScreens().stream()
            .map(sr -> Screen.builder()
                .theatre(theatre)
                .name(sr.getName())
                .totalSeats(sr.getTotalSeats())
                .seatLayout(sr.getSeatLayout() != null ? sr.getSeatLayout() : Map.of())
                .build())
            .collect(Collectors.toList());

        theatre.setScreens(screens);
        Theatre saved = theatreRepository.save(theatre);
        log.info("Theatre created: {} by partner {}", saved.getTheatreId(), partnerId);
        return toTheatreResponse(saved);
    }

    public TheatreResponse getTheatre(UUID theatreId) {
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> TheatreServiceException.notFound("Theatre", theatreId));
        return toTheatreResponse(theatre);
    }

    @Transactional
    public TheatreResponse approveTheatre(UUID theatreId, ApprovalRequest request) {
        Theatre theatre = theatreRepository.findById(theatreId)
            .orElseThrow(() -> TheatreServiceException.notFound("Theatre", theatreId));

        if (theatre.getStatus() != TheatreStatus.PENDING_APPROVAL) {
            throw TheatreServiceException.invalidTransition(
                theatre.getStatus().name(), request.getStatus().name());
        }

        TheatreStatus newStatus = request.getStatus();
        if (newStatus != TheatreStatus.APPROVED && newStatus != TheatreStatus.REJECTED) {
            throw TheatreServiceException.invalidTransition(
                theatre.getStatus().name(), newStatus.name());
        }

        theatre.setStatus(newStatus);
        Theatre saved = theatreRepository.save(theatre);
        log.info("Theatre {} status changed to {}", theatreId, newStatus);
        return toTheatreResponse(saved);
    }

    // ─── Screen ─────────────────────────────────────────────────────────────────

    @Transactional
    public ScreenResponse addScreen(UUID theatreId, ScreenRequest request, UUID partnerId) {
        Theatre theatre = getOwnedTheatre(theatreId, partnerId);

        Screen screen = Screen.builder()
            .theatre(theatre)
            .name(request.getName())
            .totalSeats(request.getTotalSeats())
            .seatLayout(request.getSeatLayout() != null ? request.getSeatLayout() : Map.of())
            .build();

        Screen saved = screenRepository.save(screen);
        log.info("Screen {} added to theatre {}", saved.getScreenId(), theatreId);
        return toScreenResponse(saved);
    }

    @Transactional
    public ScreenResponse updateScreen(UUID theatreId, UUID screenId,
                                       ScreenRequest request, UUID partnerId) {
        getOwnedTheatre(theatreId, partnerId);
        Screen screen = screenRepository.findByScreenIdAndTheatre_TheatreId(screenId, theatreId)
            .orElseThrow(() -> TheatreServiceException.notFound("Screen", screenId));

        screen.setName(request.getName());
        screen.setTotalSeats(request.getTotalSeats());
        if (request.getSeatLayout() != null) {
            screen.setSeatLayout(request.getSeatLayout());
        }

        Screen saved = screenRepository.save(screen);
        return toScreenResponse(saved);
    }

    @Transactional
    public void deleteScreen(UUID theatreId, UUID screenId, UUID partnerId) {
        getOwnedTheatre(theatreId, partnerId);
        Screen screen = screenRepository.findByScreenIdAndTheatre_TheatreId(screenId, theatreId)
            .orElseThrow(() -> TheatreServiceException.notFound("Screen", screenId));
        screenRepository.delete(screen);
        log.info("Screen {} deleted from theatre {}", screenId, theatreId);
    }

    // ─── Show ────────────────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowResponse createShow(UUID theatreId, ShowRequest request, UUID partnerId) {
        getOwnedTheatre(theatreId, partnerId);
        Screen screen = screenRepository.findByScreenIdAndTheatre_TheatreId(
                request.getScreenId(), theatreId)
            .orElseThrow(() -> TheatreServiceException.notFound("Screen", request.getScreenId()));

        if (showRepository.existsByScreen_ScreenIdAndShowDateAndShowTime(
                screen.getScreenId(), request.getShowDate(), request.getShowTime())) {
            throw TheatreServiceException.conflict(
                "A show already exists for this screen at " + request.getShowDate() + " " + request.getShowTime());
        }

        Show show = Show.builder()
            .screen(screen)
            .movieName(request.getMovieName())
            .showDate(request.getShowDate())
            .showTime(request.getShowTime())
            .basePrice(request.getBasePrice())
            .language(request.getLanguage())
            .genre(request.getGenre())
            .build();

        Show saved = showRepository.save(show);
        log.info("Show {} created for theatre {}", saved.getShowId(), theatreId);
        return toShowResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowResponse updateShow(UUID showId, ShowRequest request, UUID partnerId) {
        Show show = showRepository.findByShowIdAndScreen_Theatre_PartnerId(showId, partnerId)
            .orElseThrow(() -> TheatreServiceException.notFound("Show", showId));

        // Check for time conflict if date/time changed
        if (!show.getShowDate().equals(request.getShowDate()) ||
            !show.getShowTime().equals(request.getShowTime())) {
            if (showRepository.existsByScreen_ScreenIdAndShowDateAndShowTime(
                    show.getScreen().getScreenId(), request.getShowDate(), request.getShowTime())) {
                throw TheatreServiceException.conflict(
                    "A show already exists at " + request.getShowDate() + " " + request.getShowTime());
            }
        }

        show.setMovieName(request.getMovieName());
        show.setShowDate(request.getShowDate());
        show.setShowTime(request.getShowTime());
        show.setBasePrice(request.getBasePrice());
        show.setLanguage(request.getLanguage());
        show.setGenre(request.getGenre());

        return toShowResponse(showRepository.save(show));
    }

    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public void deleteShow(UUID showId, UUID partnerId) {
        Show show = showRepository.findByShowIdAndScreen_Theatre_PartnerId(showId, partnerId)
            .orElseThrow(() -> TheatreServiceException.notFound("Show", showId));
        showRepository.delete(show);
        log.info("Show {} deleted", showId);
    }

    // ─── Seat Inventory ──────────────────────────────────────────────────────────

    @Transactional
    public List<SeatResponse> initializeSeats(UUID showId, SeatInitRequest request, UUID partnerId) {
        Show show = showRepository.findByShowIdAndScreen_Theatre_PartnerId(showId, partnerId)
            .orElseThrow(() -> TheatreServiceException.notFound("Show", showId));

        // Validate no duplicate seat numbers in request
        Set<String> seatNumbers = new HashSet<>();
        for (var sr : request.getSeats()) {
            if (!seatNumbers.add(sr.getSeatNumber())) {
                throw TheatreServiceException.conflict(
                    "Duplicate seat number in request: " + sr.getSeatNumber());
            }
            if (seatRepository.existsByShow_ShowIdAndSeatNumber(showId, sr.getSeatNumber())) {
                throw TheatreServiceException.conflict(
                    "Seat already exists: " + sr.getSeatNumber());
            }
        }

        List<Seat> seats = request.getSeats().stream()
            .map(sr -> Seat.builder()
                .show(show)
                .seatNumber(sr.getSeatNumber())
                .status(sr.getStatus() != null ? sr.getStatus() : SeatStatus.AVAILABLE)
                .build())
            .collect(Collectors.toList());

        List<Seat> saved = seatRepository.saveAll(seats);
        log.info("Initialized {} seats for show {}", saved.size(), showId);
        return saved.stream().map(this::toSeatResponse).collect(Collectors.toList());
    }

    @Transactional
    public int bulkUpdateSeats(UUID showId, BulkSeatUpdateRequest request, UUID partnerId) {
        showRepository.findByShowIdAndScreen_Theatre_PartnerId(showId, partnerId)
            .orElseThrow(() -> TheatreServiceException.notFound("Show", showId));

        int updated = 0;
        for (SeatUpdateRequest su : request.getSeatUpdates()) {
            int rows = seatRepository.updateSeatStatus(showId, su.getSeatNumber(), su.getStatus());
            if (rows == 0) {
                throw TheatreServiceException.notFound("Seat", su.getSeatNumber());
            }
            updated += rows;
        }
        log.info("Bulk updated {} seats for show {}", updated, showId);
        return updated;
    }

    // ─── Search ──────────────────────────────────────────────────────────────────

    @Cacheable(value = "shows", key = "#city + ':' + #movieName + ':' + #showDate + ':' + #language + ':' + #genre")
    public List<TheatreSearchResponse> searchTheatres(String city, String movieName,
                                                       LocalDate showDate, String language,
                                                       String genre) {
        List<Show> shows = showRepository.searchShows(city, movieName, showDate, language, genre);

        // Group shows by theatre
        Map<UUID, List<Show>> byTheatre = shows.stream()
            .collect(Collectors.groupingBy(s -> s.getScreen().getTheatre().getTheatreId()));

        return byTheatre.entrySet().stream()
            .map(entry -> {
                Theatre theatre = entry.getValue().get(0).getScreen().getTheatre();
                List<ShowResponse> showResponses = entry.getValue().stream()
                    .map(this::toShowResponse)
                    .collect(Collectors.toList());
                return TheatreSearchResponse.builder()
                    .theatreId(theatre.getTheatreId())
                    .name(theatre.getName())
                    .city(theatre.getCity())
                    .address(theatre.getAddress())
                    .shows(showResponses)
                    .build();
            })
            .collect(Collectors.toList());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private Theatre getOwnedTheatre(UUID theatreId, UUID partnerId) {
        return theatreRepository.findByTheatreIdAndPartnerId(theatreId, partnerId)
            .orElseThrow(() -> TheatreServiceException.forbidden(
                "Theatre not found or you don't have permission to modify it"));
    }

    private TheatreResponse toTheatreResponse(Theatre theatre) {
        List<ScreenResponse> screens = theatre.getScreens().stream()
            .map(this::toScreenResponse)
            .collect(Collectors.toList());
        return TheatreResponse.builder()
            .theatreId(theatre.getTheatreId())
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
            .partnerId(theatre.getPartnerId())
            .name(theatre.getName())
            .city(theatre.getCity())
            .address(theatre.getAddress())
            .status(theatre.getStatus())
<<<<<<< HEAD
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
=======
            .screens(screens)
            .build();
    }

    private ScreenResponse toScreenResponse(Screen screen) {
        return ScreenResponse.builder()
            .screenId(screen.getScreenId())
            .name(screen.getName())
            .totalSeats(screen.getTotalSeats())
            .seatLayout(screen.getSeatLayout())
            .build();
    }

    private ShowResponse toShowResponse(Show show) {
        long available = seatRepository.countByShow_ShowIdAndStatus(
            show.getShowId(), SeatStatus.AVAILABLE);
        return ShowResponse.builder()
            .showId(show.getShowId())
            .screenId(show.getScreen().getScreenId())
            .screenName(show.getScreen().getName())
            .movieName(show.getMovieName())
            .showDate(show.getShowDate())
            .showTime(show.getShowTime())
            .basePrice(show.getBasePrice())
            .language(show.getLanguage())
            .genre(show.getGenre())
            .availableSeats(available)
            .build();
    }

    private SeatResponse toSeatResponse(Seat seat) {
        return SeatResponse.builder()
            .seatId(seat.getSeatId())
            .seatNumber(seat.getSeatNumber())
            .status(seat.getStatus())
>>>>>>> 16d3a52d46b9eb2277ae6262281c7834763fdfe3
            .build();
    }
}
