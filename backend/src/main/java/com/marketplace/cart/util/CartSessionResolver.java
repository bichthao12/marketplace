package com.marketplace.cart.util;

import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CartSessionResolver {

    public static final String GUEST_SESSION_HEADER = "X-Guest-Session-Id";

    public Optional<UUID> currentUserId() {
        return SecurityUtils.currentUser().map(UserPrincipal::getId);
    }

    public Optional<String> guestSessionId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return Optional.empty();
        }
        HttpServletRequest request = attrs.getRequest();
        String header = request.getHeader(GUEST_SESSION_HEADER);
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(header.trim());
    }

    public String cartKey() {
        Optional<UUID> userId = currentUserId();
        if (userId.isPresent()) {
            return "cart:user:" + userId.get();
        }
        String guest = guestSessionId()
                .orElseThrow(() -> new IllegalStateException("Cart session required"));
        return "cart:guest:" + guest;
    }
}
