package com.marketplace.auth.service;

import com.marketplace.auth.dto.AuthResponse;
import com.marketplace.auth.dto.ForgotPasswordRequest;
import com.marketplace.auth.dto.LoginRequest;
import com.marketplace.auth.dto.RefreshRequest;
import com.marketplace.auth.dto.RegisterRequest;
import com.marketplace.auth.dto.ResetPasswordRequest;
import com.marketplace.auth.exception.AccountSuspendedException;
import com.marketplace.auth.exception.InvalidCredentialsException;
import com.marketplace.user.entity.User;
import com.marketplace.user.repository.UserRepository;
import com.marketplace.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final PasswordResetService passwordResetService;
    private final LoginRateLimiter loginRateLimiter;
    private final String dummyHash;

    public AuthService(
            UserService userService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            PasswordResetService passwordResetService,
            LoginRateLimiter loginRateLimiter
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.passwordResetService = passwordResetService;
        this.loginRateLimiter = loginRateLimiter;
        this.dummyHash = passwordEncoder.encode("timing-attack-dummy");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        User user = userService.createUser(
                request.email(),
                request.password(),
                request.fullName(),
                request.phone()
        );
        return tokenService.createTokenPair(user, userAgent(httpRequest), clientIp(httpRequest));
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = clientIp(httpRequest);
        loginRateLimiter.checkAllowed(ip);

        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase()).orElse(null);
        String hash = user != null ? user.getPasswordHash() : dummyHash;
        boolean matches = passwordEncoder.matches(request.password(), hash);

        if (user == null || !matches) {
            loginRateLimiter.recordFailure(ip);
            throw new InvalidCredentialsException();
        }
        if (!user.isActive()) {
            throw new AccountSuspendedException();
        }

        loginRateLimiter.reset(ip);
        userService.updateLastLogin(user.getId());
        return tokenService.createTokenPair(user, userAgent(httpRequest), ip);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        var stored = tokenService.findStoredRefreshToken(request.refreshToken());
        User user = userService.getById(stored.getUserId());
        if (!user.isActive()) {
            throw new AccountSuspendedException();
        }
        return tokenService.rotateRefreshToken(
                user,
                request.refreshToken(),
                userAgent(httpRequest),
                clientIp(httpRequest)
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua : "";
    }
}
