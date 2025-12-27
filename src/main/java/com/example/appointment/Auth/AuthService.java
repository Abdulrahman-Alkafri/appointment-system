package com.example.appointment.Auth;

import com.example.appointment.Auth.dto.*;
import com.example.appointment.User.UserModel;
import com.example.appointment.User.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        UserModel user = new UserModel();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        UserModel savedUser = userRepository.save(user);

        return generateTokens(savedUser);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        UserModel user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return generateTokens(user);
    }

    @Transactional
    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Check if refresh token is blacklisted
        String refreshJti = jwtUtil.extractJti(refreshToken);
        if (refreshJti != null && tokenBlacklistService.isRefreshTokenBlacklisted(refreshJti)) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        UserSessionModel session = userSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session);
            throw new RuntimeException("Refresh token has expired");
        }

        UserModel user = session.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        return new TokenResponse(
                newAccessToken,
                refreshToken,
                accessTokenExpiration
        );
    }

    public TokenValidationResponse validateAccessToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                TokenValidationResponse response = new TokenValidationResponse();
                response.setValid(true);
                response.setMessage("Token is valid");
                response.setEmail(email);
                response.setUserId(userId);
                response.setRole(role);

                return response;
            } else {
                return new TokenValidationResponse(false, "Token is invalid or expired");
            }
        } catch (Exception e) {
            return new TokenValidationResponse(false, "Token validation failed: " + e.getMessage());
        }
    }

    @Transactional
    public void logout(String refreshToken, String accessToken) {
        // Blacklist access token if provided
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                String accessJti = jwtUtil.extractJti(accessToken);
                Long userId = jwtUtil.extractUserId(accessToken);
                long accessTtl = jwtUtil.getRemainingTtl(accessToken);

                if (accessTtl > 0 && accessJti != null) {
                    tokenBlacklistService.blacklistAccessToken(accessJti, accessTtl);
                    tokenBlacklistService.removeUserAccessToken(userId, accessJti);
                }
            } catch (Exception e) {
                // Log but don't fail logout if access token processing fails
            }
        }

        // Blacklist refresh token and delete from database
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                String refreshJti = jwtUtil.extractJti(refreshToken);
                long refreshTtl = jwtUtil.getRemainingTtl(refreshToken);

                if (refreshTtl > 0 && refreshJti != null) {
                    tokenBlacklistService.blacklistRefreshToken(refreshJti, refreshTtl);
                }
            } catch (Exception e) {
                // Log but don't fail logout
            }

            Optional<UserSessionModel> session = userSessionRepository.findByRefreshToken(refreshToken);
            session.ifPresent(userSessionRepository::delete);
        }
    }

    @Transactional
    public void logoutAllSessions(Long userId) {
        // Blacklist all active access tokens for this user
        tokenBlacklistService.blacklistAllUserAccessTokens(userId);

        // Blacklist all refresh tokens and delete from database
        List<UserSessionModel> sessions = userSessionRepository.findAllByUserId(userId);
        for (UserSessionModel session : sessions) {
            try {
                String refreshJti = jwtUtil.extractJti(session.getRefreshToken());
                long refreshTtl = jwtUtil.getRemainingTtl(session.getRefreshToken());
                if (refreshTtl > 0 && refreshJti != null) {
                    tokenBlacklistService.blacklistRefreshToken(refreshJti, refreshTtl);
                }
            } catch (Exception e) {
                // Log but continue - token might be malformed or expired
            }
        }

        userSessionRepository.deleteByUserId(userId);
    }

    private TokenResponse generateTokens(UserModel user) {
        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        UserSessionModel session = new UserSessionModel();
        session.setRefreshToken(refreshToken);
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000));

        userSessionRepository.save(session);

        return new TokenResponse(
                accessToken,
                refreshToken,
                accessTokenExpiration
        );
    }
}
