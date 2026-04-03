package com.xyz.booking.consumer;

import com.xyz.booking.service.BookingService;
import com.xyz.common.events.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Task 5.16: Kafka consumer for payment events
 * Listens to payment.success topic and updates booking status
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final BookingService bookingService;

    /**
     * Consumes payment.success events from Kafka
     * Updates booking status to CONFIRMED, releases seat locks, publishes booking.confirmed event
     */
    @KafkaListener(topics = "payment.success", groupId = "booking-service-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment success event for booking: {}", event.getBookingId());
        
        try {
            bookingService.handlePaymentSuccess(event.getBookingId());
            log.info("Successfully processed payment success for booking: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to process payment success for booking: {}", 
                    event.getBookingId(), e);
            throw e; // Kafka will retry based on configuration
        }
    }
}
