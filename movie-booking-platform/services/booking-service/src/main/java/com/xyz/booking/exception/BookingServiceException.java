package com.xyz.booking.exception;

import com.xyz.common.exception.BaseException;

public class BookingServiceException extends BaseException {

    public BookingServiceException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public static BookingServiceException notFound(String resource, Object id) {
        return new BookingServiceException(
            resource + " not found with id: " + id, "NOT_FOUND", 404);
    }

    public static BookingServiceException conflict(String message) {
        return new BookingServiceException(message, "CONFLICT", 409);
    }

    public static BookingServiceException badRequest(String message) {
        return new BookingServiceException(message, "BAD_REQUEST", 400);
    }

    public static BookingServiceException forbidden(String message) {
        return new BookingServiceException(message, "FORBIDDEN", 403);
    }

    public static BookingServiceException seatUnavailable(String seatNumber) {
        return new BookingServiceException(
            "Seat " + seatNumber + " is not available", "SEAT_UNAVAILABLE", 409);
    }

    public static BookingServiceException holdExpired(String holdId) {
        return new BookingServiceException(
            "Hold " + holdId + " has expired or does not exist", "HOLD_EXPIRED", 400);
    }

    public static BookingServiceException bookingNotFound(Object bookingId) {
        return new BookingServiceException(
            "Booking not found with id: " + bookingId, "BOOKING_NOT_FOUND", 404);
    }

    public static BookingServiceException invalidStatus(String message) {
        return new BookingServiceException(message, "INVALID_STATUS", 400);
    }

    public static BookingServiceException seatsUnavailable(java.util.List<String> seatNumbers) {
        return new BookingServiceException(
            "Seats not available: " + String.join(", ", seatNumbers), "SEATS_UNAVAILABLE", 409);
    }

    public static BookingServiceException invalidRequest(String message) {
        return new BookingServiceException(message, "INVALID_REQUEST", 400);
    }
}
