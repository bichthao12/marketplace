package com.marketplace.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.auth.controller.AuthController;
import com.marketplace.auth.dto.AuthResponse;
import com.marketplace.auth.dto.RegisterRequest;
import com.marketplace.auth.dto.UserResponse;
import com.marketplace.auth.service.AuthService;
import com.marketplace.user.entity.Role;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void registerReturnsTokens() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.register(any(), any())).thenReturn(new AuthResponse(
                new UserResponse(userId, "test@example.com", "Test User", Set.of(Role.BUYER)),
                "access-token",
                "refresh-token",
                900
        ));

        RegisterRequest register = new RegisterRequest(
                "test@example.com",
                "securePass1",
                "Test User",
                null
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.user.roles[0]").value("BUYER"));
    }
}
