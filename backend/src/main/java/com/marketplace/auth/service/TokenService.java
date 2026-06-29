package com.marketplace.auth.service;

import com.marketplace.auth.config.AuthProperties.JwtSettings;
import com.marketplace.auth.dto.AuthResponse;
import com.marketplace.auth.entity.RefreshToken;
import com.marketplace.auth.exception.TokenExpiredException;
import com.marketplace.auth.exception.TokenInvalidException;
import com.marketplace.auth.exception.TokenReuseDetectedException;
import com.marketplace.auth.repository.RefreshTokenRepository;
import com.marketplace.auth.security.JwtTokenProvider;
import com.marketplace.auth.util.TokenHasher;
import com.marketplace.user.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtSettings jwtSettings;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(
            JwtTokenProvider jwtTokenProvider,
            JwtSettings jwtSettings,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtSettings = jwtSettings;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse createTokenPair(User user, String userAgent, String ipAddress) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                new ArrayList<>(user.getRoles())
        );
        String refreshToken = issueRefreshToken(user.getId(), UUID.randomUUID(), userAgent, ipAddress);
        return AuthResponse.from(
                user,
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public AuthResponse rotateRefreshToken(User user, String rawRefreshToken, String userAgent, String ipAddress) {
        RefreshToken stored = findStoredRefreshToken(rawRefreshToken);
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByFamilyId(stored.getFamilyId());
            throw new TokenReuseDetectedException();
        }
        if (!stored.getUserId().equals(user.getId())) {
            throw new TokenInvalidException();
        }

        stored.setRevokedAt(Instant.now());
        refreshTokenRepository.save(stored);

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                new ArrayList<>(user.getRoles())
        );
        String newRefresh = issueRefreshToken(user.getId(), stored.getFamilyId(), userAgent, ipAddress);
        return AuthResponse.from(user, accessToken, newRefresh, jwtTokenProvider.getAccessTokenExpirationSeconds());
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        String hash = TokenHasher.sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    public RefreshToken findStoredRefreshToken(String rawRefreshToken) {
        String hash = TokenHasher.sha256(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(TokenInvalidException::new);
        if (token.isExpired()) {
            throw new TokenExpiredException();
        }
        return token;
    }

    private String issueRefreshToken(UUID userId, UUID familyId, String userAgent, String ipAddress) {
        String raw = TokenHasher.generateOpaqueToken();
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(TokenHasher.sha256(raw));
        entity.setFamilyId(familyId);
        entity.setExpiresAt(Instant.now().plusSeconds(jwtSettings.refreshTokenExpirationSeconds()));
        entity.setUserAgent(userAgent);
        entity.setIpAddress(ipAddress);
        refreshTokenRepository.save(entity);
        return raw;
    }
}
