package com.marketplace.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank
        @Size(min = 8, max = 128)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one digit")
        String password,
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone must be E.164 format")
        String phone
) {
}
