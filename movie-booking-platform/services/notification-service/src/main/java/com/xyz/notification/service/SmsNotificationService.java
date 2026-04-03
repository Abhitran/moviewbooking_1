package com.xyz.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SmsNotificationService {

    @Value("${notification.sms.stub-mode:true}")
    private boolean stubMode;

    public void sendBookingConfirmationSms(UUID userId, UUID bookingId, UUID showId) {
        if (stubMode) {
            log.info("=== SMS NOTIFICATION (STUB) ===");
            log.info("To User: {}", userId);
            log.info("Message: Your booking {} for show {} has been confirmed!", bookingId, showId);
            log.info("===============================");
        } else {
            // Real SMS sending logic would go here
            log.info("Sending real SMS to user {} for booking {}", userId, bookingId);
        }
    }

    public void sendBookingCancellationSms(UUID userId, UUID bookingId) {
        if (stubMode) {
            log.info("=== SMS NOTIFICATION (STUB) ===");
            log.info("To User: {}", userId);
            log.info("Message: Your booking {} has been cancelled. Refund will be processed.", bookingId);
            log.info("===============================");
        } else {
            log.info("Sending real cancellation SMS to user {} for booking {}", userId, bookingId);
        }
    }

    public void sendPaymentFailureSms(UUID bookingId, UUID paymentId) {
        if (stubMode) {
            log.info("=== SMS NOTIFICATION (STUB) ===");
            log.info("Message: Payment {} for booking {} has failed. Please try again.", paymentId, bookingId);
            log.info("===============================");
        } else {
            log.info("Sending real payment failure SMS for payment {}", paymentId);
        }
    }
}
