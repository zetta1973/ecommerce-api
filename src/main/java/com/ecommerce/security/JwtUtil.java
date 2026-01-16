package com.ecommerce.security;

import com.ecommerce.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class JwtUtil {

    private static final Key jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final Key refreshTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    public static final long JWT_EXPIRATION = 1000 * 60 * 60 * 24; // 24 horas
    public static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 días

    public static String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole() != null ? user.getRole().getName() : "USER")
            .claim("authorities", user.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toArray(String[]::new))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(jwtKey)
            .compact();
    }

    public static String generateRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
            .signWith(refreshTokenKey)
            .compact();
    }

    public static String extractEmail(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    public static String extractEmailFromRefreshToken(String refreshToken) {
        return Jwts.parserBuilder()
            .setSigningKey(refreshTokenKey)
            .build()
            .parseClaimsJws(refreshToken)
            .getBody()
            .getSubject();
    }

    public static String extractRole(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtKey)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .get("role", String.class);
    }

    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Métodos que faltaban
    public static long getExpirationTime() {
        return JWT_EXPIRATION;
    }
}
