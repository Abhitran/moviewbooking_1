package com.xyz.auth.service;

import com.xyz.auth.dto.LoginResponse;
import com.xyz.auth.entity.User;
import com.xyz.auth.entity.UserRole;
import com.xyz.auth.exception.AuthServiceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {

    private final KeyPair keyPair;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.access-token-expiry:900000}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    public JwtService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.keyPair = generateKeyPair();
        log.info("RSA key pair generated for JWT signing");
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiry);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(user.getUserId().toString())
                .claim(CLAIM_USER_ID, user.getUserId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim("email", user.getEmail())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(keyPair.getPrivate())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiry);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(user.getUserId().toString())
                .claim(CLAIM_USER_ID, user.getUserId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(keyPair.getPrivate())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (isTokenBlacklisted(token)) {
                throw AuthServiceException.tokenBlacklisted();
            }

            return claims;
        } catch (AuthServiceException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            throw AuthServiceException.invalidToken();
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = validateToken(token);
        return UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
    }

    public UserRole extractRole(String token) {
        Claims claims = validateToken(token);
        return UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
    }

    public Date getTokenExpiry(String token) {
        Claims claims = validateToken(token);
        return claims.getExpiration();
    }

    public LoginResponse refreshToken(String refreshToken) {
        Claims claims = validateToken(refreshToken);

        String tokenType = claims.get(CLAIM_TYPE, String.class);
        if (!TYPE_REFRESH.equals(tokenType)) {
            throw AuthServiceException.invalidToken();
        }

        UUID userId = UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
        UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));

        // Build a minimal User object for token generation
        User user = User.builder()
                .userId(userId)
                .role(role)
                .email(claims.get("email", String.class) != null
                        ? claims.get("email", String.class) : "")
                .build();

        String newAccessToken = generateAccessToken(user);
        Date expiry = getTokenExpiryFromClaims(newAccessToken);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiry / 1000)
                .userId(userId)
                .role(role)
                .build();
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String jti = claims.getId();
            Date expiry = claims.getExpiration();
            long ttlMillis = expiry.getTime() - System.currentTimeMillis();

            if (ttlMillis > 0) {
                String key = BLACKLIST_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "blacklisted", ttlMillis, TimeUnit.MILLISECONDS);
                log.debug("Token blacklisted with jti: {}", jti);
            }
        } catch (JwtException e) {
            log.warn("Attempted to blacklist invalid token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String jti = claims.getId();
            String key = BLACKLIST_PREFIX + jti;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (JwtException e) {
            return false;
        }
    }

    private Date getTokenExpiryFromClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (JwtException e) {
            return new Date();
        }
    }
}
