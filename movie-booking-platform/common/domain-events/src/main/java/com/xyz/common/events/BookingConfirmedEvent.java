package com.xyz.common.events;

import java.util.UUID;

public class BookingConfirmedEvent extends DomainEvent {
    private UUID bookingId;
    private UUID userId;
    private UUID showId;
    private String userEmail;
    private double totalAmount;

    public BookingConfirmedEvent() {
        super("BOOKING_CONFIRMED");
    }

    public BookingConfirmedEvent(UUID bookingId, UUID userId, UUID showId, String userEmail, double totalAmount) {
        super("BOOKING_CONFIRMED");
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.userEmail = userEmail;
        this.totalAmount = totalAmount;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getShowId() {
        return showId;
    }

    public void setShowId(UUID showId) {
        this.showId = showId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
