package com.xyz.auth.exception;

import com.xyz.common.exception.BaseException;

public class AuthServiceException extends BaseException {

    public AuthServiceException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public static AuthServiceException emailAlreadyExists(String email) {
        return new AuthServiceException(
                "User with email '" + email + "' already exists",
                "EMAIL_ALREADY_EXISTS",
                409
        );
    }

    public static AuthServiceException invalidCredentials() {
        return new AuthServiceException(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                401
        );
    }

    public static AuthServiceException invalidToken() {
        return new AuthServiceException(
                "Invalid or expired token",
                "INVALID_TOKEN",
                401
        );
    }

    public static AuthServiceException tokenBlacklisted() {
        return new AuthServiceException(
                "Token has been invalidated",
                "TOKEN_BLACKLISTED",
                401
        );
    }
}
