package com.example.appointment.Auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public JwtUtil(TokenBlacklistService tokenBlacklistService) {
        this.signingKey = Jwts.SIG.HS256.key().build();
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    public String generateAccessToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("jti", UUID.randomUUID().toString());

        String token = createToken(claims, email, accessTokenExpiration);

        // Track this access token for the user
        String jti = extractJti(token);
        tokenBlacklistService.trackUserAccessToken(userId, jti, accessTokenExpiration);

        return token;
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", UUID.randomUUID().toString());
        return createToken(claims, email, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getRemainingTtl(String token) {
        Date expiration = extractExpiration(token);
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        if (!tokenEmail.equals(email) || isTokenExpired(token)) {
            return false;
        }

        // Check if token is blacklisted
        String jti = extractJti(token);
        if (jti != null && tokenBlacklistService.isAccessTokenBlacklisted(jti)) {
            return false;
        }

        return true;
    }

    public Boolean validateToken(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }

            // Check if token is blacklisted
            String jti = extractJti(token);
            if (jti != null && tokenBlacklistService.isAccessTokenBlacklisted(jti)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
