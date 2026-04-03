package com.xyz.booking.service;

import com.xyz.booking.client.PaymentServiceClient;
import com.xyz.booking.client.TheatreServiceClient;
import com.xyz.booking.dto.*;
import com.xyz.booking.entity.Booking;
import com.xyz.booking.entity.BookingSeat;
import com.xyz.booking.entity.BookingStatus;
import com.xyz.booking.exception.BookingServiceException;
import com.xyz.booking.offer.BookingContext;
import com.xyz.booking.offer.DiscountResult;
import com.xyz.booking.offer.OfferEngine;
import com.xyz.booking.repository.BookingRepository;
import com.xyz.common.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final PaymentServiceClient paymentServiceClient;
    private final TheatreServiceClient theatreServiceClient;
    private final OfferEngine offerEngine;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${booking.seat-hold.ttl-seconds:600}")
    private long holdTtlSeconds;

    /**
     * Task 5.14: Confirm booking endpoint
     * Validates holdId, calculates offers, initiates payment, creates booking with PENDING status
     */
    @Transactional
    public BookingResponse confirmBooking(String holdId, UUID userId) {
        // Validate hold exists and not expired
        HoldData holdData = getHoldData(holdId);
        if (holdData == null) {
            throw BookingServiceException.holdExpired(holdId);
        }

        // Fetch show details from theatre service (for show time to calculate offers)
        Map<String, Object> showDetails = fetchShowDetails(holdData.getShowId());
        LocalTime showTime = extractShowTime(showDetails);

        // Calculate offers using OfferEngine
        BookingContext context = BookingContext.builder()
            .seatPrices(holdData.getSeatPrices())
            .showTime(showTime)
            .build();
        
        DiscountResult discount = offerEngine.calculateBestOffer(context);

        // Calculate amounts
        BigDecimal totalAmount = holdData.getSeatPrices().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = discount.getDiscountAmount();
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // Create booking record with PENDING status
        Booking booking = Booking.builder()
            .userId(userId)
            .showId(holdData.getShowId())
            .totalAmount(totalAmount)
            .discountAmount(discountAmount)
            .finalAmount(finalAmount)
            .status(BookingStatus.PENDING)
            .offerApplied(discount.getOfferCode())
            .build();

        // Create booking seats
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (int i = 0; i < holdData.getSeatNumbers().size(); i++) {
            BookingSeat seat = BookingSeat.builder()
                .booking(booking)
                .seatNumber(holdData.getSeatNumbers().get(i))
                .price(holdData.getSeatPrices().get(i))
                .discountApplied(BigDecimal.ZERO)
                .build();
            bookingSeats.add(seat);
        }
        booking.setSeats(bookingSeats);

        booking = bookingRepository.save(booking);

        // Call Payment Service to initiate payment
        PaymentInitiateRequest paymentRequest = PaymentInitiateRequest.builder()
            .bookingId(booking.getBookingId())
            .amount(finalAmount)
            .userId(userId)
            .build();

        PaymentInitiateResponse paymentResponse = paymentServiceClient.initiatePayment(paymentRequest);
        log.info("Payment initiated for booking {}: paymentId={}", booking.getBookingId(), paymentResponse.getPaymentId());

        // Delete hold from Redis (it's now converted to booking)
        deleteHold(holdId);

        return toBookingResponse(booking);
    }

    /**
     * Task 5.18: Cancel booking endpoint
     * Validates booking exists and is CONFIRMED, updates status to CANCELLED, initiates refund
     */
    @Transactional
    public CancellationResponse cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findByBookingIdAndUserId(bookingId, userId)
            .orElseThrow(() -> BookingServiceException.bookingNotFound(bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw BookingServiceException.invalidStatus(
                "Cannot cancel booking with status: " + booking.getStatus());
        }

        // Update booking status to CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Initiate refund (we need to find the payment ID - for now we'll use bookingId as reference)
        RefundRequest refundRequest = RefundRequest.builder()
            .paymentId(bookingId) // In real scenario, we'd store paymentId in booking
            .amount(booking.getFinalAmount())
            .build();

        paymentServiceClient.initiateRefund(refundRequest);

        // Publish booking.cancelled event to Kafka
        Map<String, Object> cancelEvent = new HashMap<>();
        cancelEvent.put("bookingId", bookingId);
        cancelEvent.put("userId", userId);
        cancelEvent.put("refundAmount", booking.getFinalAmount());
        kafkaTemplate.send("booking.cancelled", cancelEvent);

        log.info("Booking {} cancelled, refund initiated", bookingId);

        return CancellationResponse.builder()
            .bookingId(bookingId)
            .refundAmount(booking.getFinalAmount())
            .status("CANCELLED")
            .build();
    }

    /**
     * Task 5.20: Bulk booking endpoint
     * Accepts multiple seat selections, executes all in single transaction
     */
    @Transactional
    public List<BookingResponse> bulkBooking(List<HoldRequest> requests, UUID userId) {
        List<BookingResponse> responses = new ArrayList<>();

        try {
            for (HoldRequest request : requests) {
                // Try to acquire locks for all seats
                boolean locked = seatLockService.tryAcquireAll(
                    request.getShowId(), 
                    request.getSeatNumbers(), 
                    userId
                );

                if (!locked) {
                    throw BookingServiceException.seatsUnavailable(request.getSeatNumbers());
                }

                // Create hold and confirm booking immediately
                String holdId = createHold(request.getShowId(), request.getSeatNumbers(), userId);
                BookingResponse response = confirmBooking(holdId, userId);
                responses.add(response);
            }

            return responses;
        } catch (Exception e) {
            // Transaction will rollback automatically due to @Transactional
            log.error("Bulk booking failed, rolling back: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Task 5.22: Bulk cancellation endpoint
     * Cancels all bookings in single transaction, rollback if any fails
     */
    @Transactional
    public List<CancellationResponse> bulkCancellation(List<UUID> bookingIds, UUID userId) {
        List<CancellationResponse> responses = new ArrayList<>();

        // Validate all bookings exist and belong to user
        List<Booking> bookings = bookingRepository.findByBookingIdInAndUserId(bookingIds, userId);
        if (bookings.size() != bookingIds.size()) {
            throw BookingServiceException.invalidRequest("Some bookings not found or don't belong to user");
        }

        try {
            for (UUID bookingId : bookingIds) {
                CancellationResponse response = cancelBooking(bookingId, userId);
                responses.add(response);
            }
            return responses;
        } catch (Exception e) {
            // Transaction will rollback automatically
            log.error("Bulk cancellation failed, rolling back: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Task 5.24: User bookings query endpoint
     * Returns all bookings with pagination (page size: 20)
     */
    public Page<BookingResponse> getUserBookings(UUID userId, int page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageRequest);
        return bookings.map(this::toBookingResponse);
    }

    /**
     * Task 5.16: Kafka consumer for payment success events
     * Updates booking status to CONFIRMED, releases seat locks, publishes booking.confirmed event
     */
    @Transactional
    public void handlePaymentSuccess(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> BookingServiceException.bookingNotFound(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Booking {} is not in PENDING status, skipping confirmation", bookingId);
            return;
        }

        // Update booking status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Release seat locks from Redis
        List<String> seatNumbers = booking.getSeats().stream()
            .map(BookingSeat::getSeatNumber)
            .toList();
        seatLockService.releaseAll(booking.getShowId(), seatNumbers, booking.getUserId());

        // Publish booking.confirmed event to Kafka
        BookingConfirmedEvent event = new BookingConfirmedEvent(
            booking.getBookingId(),
            booking.getUserId(),
            booking.getShowId(),
            "user@example.com", // In real scenario, fetch from user service
            booking.getFinalAmount().doubleValue()
        );
        kafkaTemplate.send("booking.confirmed", event);

        log.info("Booking {} confirmed and notification event published", bookingId);
    }

    // Helper methods

    private String createHold(UUID showId, List<String> seatNumbers, UUID userId) {
        String holdId = UUID.randomUUID().toString();
        
        // Fetch seat prices from theatre service
        List<BigDecimal> seatPrices = fetchSeatPrices(showId, seatNumbers);
        
        HoldData holdData = new HoldData(showId, seatNumbers, seatPrices, userId);
        
        // Store hold in Redis with TTL
        RBucket<HoldData> bucket = redissonClient.getBucket("hold:" + holdId);
        bucket.set(holdData, Duration.ofSeconds(holdTtlSeconds));
        
        return holdId;
    }

    private HoldData getHoldData(String holdId) {
        RBucket<HoldData> bucket = redissonClient.getBucket("hold:" + holdId);
        return bucket.get();
    }

    private void deleteHold(String holdId) {
        redissonClient.getBucket("hold:" + holdId).delete();
    }

    private List<BigDecimal> fetchSeatPrices(UUID showId, List<String> seatNumbers) {
        // For simplicity, return base price for all seats
        // In real scenario, fetch from theatre service
        return seatNumbers.stream()
            .map(s -> new BigDecimal("150.00"))
            .collect(Collectors.toList());
    }

    private Map<String, Object> fetchShowDetails(UUID showId) {
        // Stub: return mock show details
        Map<String, Object> show = new HashMap<>();
        show.put("showId", showId);
        show.put("showTime", "14:30:00");
        return show;
    }

    private LocalTime extractShowTime(Map<String, Object> showDetails) {
        String timeStr = (String) showDetails.get("showTime");
        return LocalTime.parse(timeStr);
    }

    private BookingResponse toBookingResponse(Booking booking) {
        List<String> seatNumbers = booking.getSeats().stream()
            .map(BookingSeat::getSeatNumber)
            .toList();

        return BookingResponse.builder()
            .bookingId(booking.getBookingId())
            .userId(booking.getUserId())
            .showId(booking.getShowId())
            .seats(seatNumbers)
            .totalAmount(booking.getTotalAmount())
            .discountAmount(booking.getDiscountAmount())
            .finalAmount(booking.getFinalAmount())
            .offerApplied(booking.getOfferApplied())
            .status(booking.getStatus())
            .createdAt(booking.getCreatedAt())
            .build();
    }

    // Inner class for hold data storage
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class HoldData implements java.io.Serializable {
        private UUID showId;
        private List<String> seatNumbers;
        private List<BigDecimal> seatPrices;
        private UUID userId;
    }
}
