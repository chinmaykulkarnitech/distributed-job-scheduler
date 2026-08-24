package com.chinmaytech.job_scheduler_backend.security;

import io.jsonwebtoken.Jwts;


import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

    @Service
    public class JwtService {

        private final SecretKey secretKey;
        private final long expirationMs;

        public JwtService(
                @Value("${jwt.secret}") String secret,
                @Value("${jwt.expiration}") long expirationMs) {

            this.secretKey = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8)
            );

            this.expirationMs = expirationMs;
        }

        public String generateToken(String userId, String email) {

            Date now = new Date();

            Date expiration = new Date(
                    now.getTime() + expirationMs
            );

            return Jwts.builder()
                    .subject(userId)
                    .claim("email", email)
                    .issuedAt(now)
                    .expiration(expiration)
                    .signWith(secretKey)
                    .compact();
        }

        public String extractUserId(String token) {

            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        }

        public boolean isValid(String token) {

            try {

                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token);

                return true;

            } catch (Exception e) {

                return false;
            }
        }
    }

