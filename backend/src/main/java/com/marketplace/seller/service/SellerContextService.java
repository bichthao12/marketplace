package com.marketplace.seller.service;

import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.exception.SellerNotApprovedException;
import com.marketplace.seller.exception.SellerProfileNotFoundException;
import com.marketplace.seller.repository.SellerProfileRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerContextService {

    private final SellerProfileRepository sellerProfileRepository;

    public SellerContextService(SellerProfileRepository sellerProfileRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
    }

    @Transactional(readOnly = true)
    public SellerProfile requireCurrentSellerProfile() {
        UUID userId = currentUserId();
        SellerProfile profile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(SellerProfileNotFoundException::new);
        if (!profile.isApproved()) {
            throw new SellerNotApprovedException();
        }
        return profile;
    }

    @Transactional(readOnly = true)
    public SellerProfile findCurrentSellerProfile() {
        UUID userId = currentUserId();
        return sellerProfileRepository.findByUserId(userId)
                .orElseThrow(SellerProfileNotFoundException::new);
    }

    private static UUID currentUserId() {
        UserPrincipal principal = SecurityUtils.currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
        return principal.getId();
    }
}
