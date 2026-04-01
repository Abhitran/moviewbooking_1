package com.xyz.common.exception;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }
}
