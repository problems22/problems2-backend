package org.example.problems2backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.problems2backend.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService
{
    @Value("${spring.application.jwt_access_secret}")
    private String jwtAccessSecret;

    @Value("${spring.application.jwt_refresh_secret}")
    private String jwtRefreshSecret;

    @Value("${spring.application.jwt_access_expiration}")
    private Long jwtAccessExpiration;

    @Value("${spring.application.jwt_refresh_expiration}")
    private Long jwtRefreshExpiration;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtAccessSecret.getBytes()))
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes()))
                .compact();
    }


    public String extractUsername(String token, boolean isAccessToken) {
        return extractClaim(token, Claims::getSubject, isAccessToken);
    }


    public boolean isTokenExpired(String token, boolean isAccessToken) {
        return extractExpiration(token, isAccessToken).before(new Date());
    }

    private Date extractExpiration(String token, boolean isAccessToken) {
        return extractClaim(token, Claims::getExpiration, isAccessToken);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isAccessToken) {
        Claims claims = extractAllClaims(token, isAccessToken);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails, boolean isAccessToken) {
        String username = extractUsername(token, isAccessToken);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, isAccessToken);
    }

    private Claims extractAllClaims(String token, boolean isAccessToken) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(isAccessToken ?
                        jwtAccessSecret.getBytes() :
                        jwtRefreshSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
