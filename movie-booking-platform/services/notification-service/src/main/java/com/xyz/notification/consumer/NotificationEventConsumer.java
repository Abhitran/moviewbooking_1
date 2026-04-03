package com.xyz.notification.consumer;

import com.xyz.common.events.BookingConfirmedEvent;
import com.xyz.common.events.PaymentFailedEvent;
import com.xyz.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "booking.confirmed", groupId = "notification-service")
    public void handleBookingConfirmed(BookingConfirmedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received booking.confirmed event for bookingId: {}", event.getBookingId());
            notificationService.sendBookingConfirmation(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed booking.confirmed event for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking.confirmed event for bookingId: {}", event.getBookingId(), e);
            // Don't acknowledge - message will be retried
            throw e;
        }
    }

    @KafkaListener(topics = "booking.cancelled", groupId = "notification-service")
    public void handleBookingCancelled(BookingConfirmedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received booking.cancelled event for bookingId: {}", event.getBookingId());
            notificationService.sendBookingCancellation(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed booking.cancelled event for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking.cancelled event for bookingId: {}", event.getBookingId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "payment.failed", groupId = "notification-service")
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment acknowledgment) {
        try {
            log.info("Received payment.failed event for paymentId: {}", event.getPaymentId());
            notificationService.sendPaymentFailure(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed payment.failed event for paymentId: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("Error processing payment.failed event for paymentId: {}", event.getPaymentId(), e);
            throw e;
        }
    }
}
