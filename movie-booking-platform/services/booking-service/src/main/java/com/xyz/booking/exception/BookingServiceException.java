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
}
