package com.xyz.auth.service;

import com.xyz.auth.dto.*;
import com.xyz.auth.entity.User;
import com.xyz.auth.entity.UserRole;
import com.xyz.auth.exception.AuthServiceException;
import com.xyz.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.debug("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthServiceException.emailAlreadyExists(request.getEmail());
        }

        UserRole role = request.getRole() != null ? request.getRole() : UserRole.CUSTOMER;

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: {}", saved.getUserId());

        return RegisterResponse.builder()
                .userId(saved.getUserId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(AuthServiceException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthServiceException.invalidCredentials();
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("User logged in successfully: {}", user.getUserId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(900)
                .userId(user.getUserId())
                .role(user.getRole())
                .build();
    }

    public void logout(String token) {
        log.debug("Logging out token");
        jwtService.blacklistToken(token);
        log.info("Token blacklisted successfully");
    }
}
