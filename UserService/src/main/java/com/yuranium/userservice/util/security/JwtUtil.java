package com.yuranium.userservice.util.security;

import com.yuranium.userservice.enums.RoleType;
import com.yuranium.userservice.models.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil
{
    @Value("${auth.jwt.lifetime}")
    private Duration jwtLifetime;

    public static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;

    public String generateToken(UserEntity user)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());

        Date issuedDate = new Date();
        Date expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token)
    {
        return getAllClaims(token).getSubject();
    }

    public RoleType getRole(String token)
    {
        return RoleType.valueOf(getAllClaims(token)
                .get("role", String.class));
    }

    public Claims getAllClaims(String token)
    {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isValidToken(String authHeader)
    {
        String token = null;
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX))
            return false;
        try
        {
            token = authHeader.substring(BEARER_PREFIX.length());
            getAllClaims(token);

        } catch (JwtException | IllegalArgumentException exc)
        {
            return false;
        }
        return true;
    }
}