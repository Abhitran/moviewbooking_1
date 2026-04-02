package com.xyz.theatre.exception;

import com.xyz.common.exception.BaseException;

public class TheatreServiceException extends BaseException {

    public TheatreServiceException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public static TheatreServiceException notFound(String resource, Object id) {
        return new TheatreServiceException(
            resource + " not found with id: " + id, "NOT_FOUND", 404);
    }

    public static TheatreServiceException forbidden(String message) {
        return new TheatreServiceException(message, "FORBIDDEN", 403);
    }

    public static TheatreServiceException conflict(String message) {
        return new TheatreServiceException(message, "CONFLICT", 409);
    }

    public static TheatreServiceException invalidTransition(String from, String to) {
        return new TheatreServiceException(
            "Cannot transition from " + from + " to " + to, "INVALID_STATUS_TRANSITION", 400);
    }
}
