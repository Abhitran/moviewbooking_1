package com.xyz.common.events;

import java.util.UUID;

public class PaymentFailedEvent extends DomainEvent {
    private UUID paymentId;
    private UUID bookingId;
    private String reason;

    public PaymentFailedEvent() {
        super("PAYMENT_FAILED");
    }

    public PaymentFailedEvent(UUID paymentId, UUID bookingId, String reason) {
        super("PAYMENT_FAILED");
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.reason = reason;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
