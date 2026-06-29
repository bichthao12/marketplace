package com.marketplace.auth.dto;

import com.marketplace.user.entity.Role;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Set<Role> roles
) {
}
