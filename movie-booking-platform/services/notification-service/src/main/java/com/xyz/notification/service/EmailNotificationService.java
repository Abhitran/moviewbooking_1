package com.xyz.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class EmailNotificationService {

    @Value("${notification.email.stub-mode:true}")
    private boolean stubMode;

    public void sendBookingConfirmationEmail(String email, UUID bookingId, UUID showId, double amount) {
        if (stubMode) {
            log.info("=== EMAIL NOTIFICATION (STUB) ===");
            log.info("To: {}", email);
            log.info("Subject: Booking Confirmation - {}", bookingId);
            log.info("Body: Your booking has been confirmed!");
            log.info("  Booking ID: {}", bookingId);
            log.info("  Show ID: {}", showId);
            log.info("  Total Amount: ${}", amount);
            log.info("================================");
        } else {
            // Real email sending logic would go here
            log.info("Sending real email to {} for booking {}", email, bookingId);
        }
    }

    public void sendBookingCancellationEmail(String email, UUID bookingId, double refundAmount) {
        if (stubMode) {
            log.info("=== EMAIL NOTIFICATION (STUB) ===");
            log.info("To: {}", email);
            log.info("Subject: Booking Cancellation - {}", bookingId);
            log.info("Body: Your booking has been cancelled.");
            log.info("  Booking ID: {}", bookingId);
            log.info("  Refund Amount: ${}", refundAmount);
            log.info("================================");
        } else {
            log.info("Sending real cancellation email to {} for booking {}", email, bookingId);
        }
    }

    public void sendPaymentFailureEmail(UUID bookingId, UUID paymentId, String reason) {
        if (stubMode) {
            log.info("=== EMAIL NOTIFICATION (STUB) ===");
            log.info("Subject: Payment Failed - {}", paymentId);
            log.info("Body: Your payment has failed.");
            log.info("  Booking ID: {}", bookingId);
            log.info("  Payment ID: {}", paymentId);
            log.info("  Reason: {}", reason);
            log.info("================================");
        } else {
            log.info("Sending real payment failure email for payment {}", paymentId);
        }
    }
}
