package com.marketplace.auth.security;

import com.marketplace.auth.config.AuthProperties.JwtSettings;
import com.marketplace.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtSettings settings;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtSettings settings) {
        this.settings = settings;
        this.secretKey = buildKey(settings.secret());
    }

    public String createAccessToken(UUID userId, String email, List<Role> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(settings.accessTokenExpirationSeconds());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userId.toString())
                .issuer(settings.issuer())
                .claim("email", email)
                .claim("roles", roles.stream().map(Role::name).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(settings.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException ex) {
            throw ex;
        }
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public long getAccessTokenExpirationSeconds() {
        return settings.accessTokenExpirationSeconds();
    }

    private static SecretKey buildKey(String secret) {
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
