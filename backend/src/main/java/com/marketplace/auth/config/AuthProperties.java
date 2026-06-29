package com.marketplace.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthProperties {

    @Bean
    public JwtSettings jwtSettings(
            @Value("${app.auth.jwt.secret}") String secret,
            @Value("${app.auth.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.auth.jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${app.auth.jwt.issuer}") String issuer
    ) {
        return new JwtSettings(secret, accessTokenExpiration, refreshTokenExpiration, issuer);
    }

    @Bean
    public PasswordResetSettings passwordResetSettings(
            @Value("${app.auth.password-reset.expiration}") long expirationSeconds,
            @Value("${app.frontend.buyer-url}") String buyerUrl,
            @Value("${app.frontend.reset-password-path}") String resetPasswordPath
    ) {
        return new PasswordResetSettings(expirationSeconds, buyerUrl, resetPasswordPath);
    }

    public record JwtSettings(
            String secret,
            long accessTokenExpirationSeconds,
            long refreshTokenExpirationSeconds,
            String issuer
    ) {
    }

    public record PasswordResetSettings(
            long expirationSeconds,
            String buyerUrl,
            String resetPasswordPath
    ) {
        public String buildResetUrl(String plainToken) {
            return buyerUrl + resetPasswordPath + "?token=" + plainToken;
        }
    }
}
