package com.marketplace.auth.dto;

import com.marketplace.user.entity.User;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        long expiresIn
) {

    public static AuthResponse from(User user, String accessToken, String refreshToken, long expiresIn) {
        return new AuthResponse(
                new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles()),
                accessToken,
                refreshToken,
                expiresIn
        );
    }
}
