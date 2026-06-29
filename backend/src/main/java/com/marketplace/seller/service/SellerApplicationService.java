package com.marketplace.seller.service;

import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.util.SlugUtils;
import com.marketplace.common.exception.ApiException;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.order.entity.OrderGroupStatus;
import com.marketplace.order.repository.OrderGroupRepository;
import com.marketplace.seller.dto.SellerApplicationRequest;
import com.marketplace.seller.dto.SellerApplicationResponse;
import com.marketplace.seller.dto.SellerProfileResponse;
import com.marketplace.seller.dto.UpdateSellerProfileRequest;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.entity.SellerStatus;
import com.marketplace.seller.exception.SellerProfileNotFoundException;
import com.marketplace.seller.repository.SellerProfileRepository;
import com.marketplace.user.entity.Role;
import com.marketplace.user.service.UserService;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerApplicationService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderGroupRepository orderGroupRepository;

    public SellerApplicationService(
            SellerProfileRepository sellerProfileRepository,
            UserService userService,
            ProductRepository productRepository,
            OrderGroupRepository orderGroupRepository
    ) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.userService = userService;
        this.productRepository = productRepository;
        this.orderGroupRepository = orderGroupRepository;
    }

    @Transactional
    public SellerApplicationResponse apply(SellerApplicationRequest request) {
        UUID userId = currentUserId();
        if (sellerProfileRepository.findByUserId(userId).isPresent()) {
            throw new ApiException("SELLER_APPLICATION_EXISTS", "Seller application already exists", HttpStatus.CONFLICT);
        }

        SellerProfile profile = new SellerProfile();
        profile.setUserId(userId);
        profile.setShopName(request.shopName().trim());
        profile.setSlug(SlugUtils.uniqueSlug(request.shopName(), sellerProfileRepository::existsBySlug));
        profile.setDescription(request.description());
        profile.setStatus(SellerStatus.PENDING);
        profile = sellerProfileRepository.save(profile);

        userService.addRole(userId, Role.SELLER);

        return new SellerApplicationResponse(
                profile.getId(),
                profile.getShopName(),
                profile.getSlug(),
                EnumFormat.toApi(profile.getStatus()),
                profile.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public SellerProfileResponse getProfile() {
        SellerProfile profile = sellerProfileRepository.findByUserId(currentUserId())
                .orElseThrow(SellerProfileNotFoundException::new);

        long totalProducts = productRepository.countBySellerId(profile.getId());
        long pendingOrders = orderGroupRepository.countBySellerIdAndStatus(profile.getId(), OrderGroupStatus.NEW);

        return new SellerProfileResponse(
                profile.getId(),
                profile.getShopName(),
                profile.getSlug(),
                profile.getDescription(),
                profile.getLogoUrl(),
                EnumFormat.toApi(profile.getStatus()),
                profile.getCurrency(),
                Map.of(
                        "totalProducts", totalProducts,
                        "pendingOrders", pendingOrders,
                        "revenue30d", 0
                )
        );
    }

    @Transactional
    public SellerProfileResponse updateProfile(UpdateSellerProfileRequest request) {
        SellerProfile profile = sellerProfileRepository.findByUserId(currentUserId())
                .orElseThrow(SellerProfileNotFoundException::new);

        if (request.description() != null) {
            profile.setDescription(request.description());
        }
        if (request.logoUrl() != null) {
            profile.setLogoUrl(request.logoUrl());
        }
        profile = sellerProfileRepository.save(profile);
        return getProfile();
    }

    private static UUID currentUserId() {
        return SecurityUtils.currentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED));
    }
}
