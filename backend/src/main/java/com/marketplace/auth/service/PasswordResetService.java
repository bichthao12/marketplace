package com.marketplace.auth.service;

import com.marketplace.auth.config.AuthProperties.PasswordResetSettings;
import com.marketplace.auth.entity.PasswordResetToken;
import com.marketplace.auth.exception.TokenExpiredException;
import com.marketplace.auth.exception.TokenInvalidException;
import com.marketplace.auth.repository.PasswordResetTokenRepository;
import com.marketplace.auth.util.TokenHasher;
import com.marketplace.user.repository.UserRepository;
import com.marketplace.user.service.UserService;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenService tokenService;
    private final PasswordResetSettings settings;

    public PasswordResetService(
            UserRepository userRepository,
            UserService userService,
            PasswordResetTokenRepository passwordResetTokenRepository,
            TokenService tokenService,
            PasswordResetSettings settings
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenService = tokenService;
        this.settings = settings;
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase()).ifPresent(user -> {
            String plainToken = TokenHasher.generateOpaqueToken();
            PasswordResetToken entity = new PasswordResetToken();
            entity.setUserId(user.getId());
            entity.setTokenHash(TokenHasher.sha256(plainToken));
            entity.setExpiresAt(Instant.now().plusSeconds(settings.expirationSeconds()));
            passwordResetTokenRepository.save(entity);

            // TODO: replace with async email queue
            log.info("Password reset link for {}: {}", user.getEmail(), settings.buildResetUrl(plainToken));
        });
    }

    @Transactional
    public void resetPassword(String plainToken, String newPassword) {
        String hash = TokenHasher.sha256(plainToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(TokenInvalidException::new);

        if (token.isUsed()) {
            throw new TokenInvalidException("Reset token already used");
        }
        if (token.isExpired()) {
            throw new TokenExpiredException();
        }

        userService.updatePassword(token.getUserId(), newPassword);
        token.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(token);
        tokenService.revokeAllForUser(token.getUserId());
    }
}
