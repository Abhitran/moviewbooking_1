package com.xyz.auth.controller;

import com.xyz.auth.dto.*;
import com.xyz.auth.exception.AuthServiceException;
import com.xyz.auth.service.JwtService;
import com.xyz.auth.service.UserService;
import com.xyz.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.debug("POST /api/auth/register - email: {}", request.getEmail());
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.debug("POST /api/auth/login - email: {}", request.getEmail());
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        log.debug("POST /api/auth/refresh");
        LoginResponse response = jwtService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        log.debug("POST /api/auth/logout");
        userService.logout(request.getAccessToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateResponse>> validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.debug("GET /api/auth/validate");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            ValidateResponse invalid = ValidateResponse.builder().valid(false).build();
            return ResponseEntity.ok(ApiResponse.success(invalid));
        }

        String token = authHeader.substring(7);

        try {
            var userId = jwtService.extractUserId(token);
            var role = jwtService.extractRole(token);

            ValidateResponse response = ValidateResponse.builder()
                    .userId(userId)
                    .role(role.name())
                    .valid(true)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (AuthServiceException e) {
            ValidateResponse invalid = ValidateResponse.builder().valid(false).build();
            return ResponseEntity.ok(ApiResponse.success(invalid));
        }
    }
}
