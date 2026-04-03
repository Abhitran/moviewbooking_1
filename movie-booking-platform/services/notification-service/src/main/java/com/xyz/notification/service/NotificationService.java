package com.xyz.notification.service;

import com.xyz.common.events.BookingConfirmedEvent;
import com.xyz.common.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        log.info("Sending booking confirmation for bookingId: {}", event.getBookingId());
        
        // Send email notification
        emailService.sendBookingConfirmationEmail(
            event.getUserEmail(),
            event.getBookingId(),
            event.getShowId(),
            event.getTotalAmount()
        );
        
        // Send SMS notification
        smsService.sendBookingConfirmationSms(
            event.getUserId(),
            event.getBookingId(),
            event.getShowId()
        );
    }

    public void sendBookingCancellation(BookingConfirmedEvent event) {
        log.info("Sending booking cancellation for bookingId: {}", event.getBookingId());
        
        // Send email notification
        emailService.sendBookingCancellationEmail(
            event.getUserEmail(),
            event.getBookingId(),
            event.getTotalAmount()
        );
        
        // Send SMS notification
        smsService.sendBookingCancellationSms(
            event.getUserId(),
            event.getBookingId()
        );
    }

    public void sendPaymentFailure(PaymentFailedEvent event) {
        log.info("Sending payment failure notification for paymentId: {}", event.getPaymentId());
        
        // Send email notification
        emailService.sendPaymentFailureEmail(
            event.getBookingId(),
            event.getPaymentId(),
            event.getReason()
        );
        
        // Send SMS notification
        smsService.sendPaymentFailureSms(
            event.getBookingId(),
            event.getPaymentId()
        );
    }
}
