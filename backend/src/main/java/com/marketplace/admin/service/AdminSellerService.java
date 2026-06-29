package com.marketplace.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.admin.dto.AdminSellerActionRequest;
import com.marketplace.admin.dto.AdminSellerResponse;
import com.marketplace.admin.entity.AuditLog;
import com.marketplace.admin.repository.AuditLogRepository;
import com.marketplace.auth.security.SecurityUtils;
import com.marketplace.auth.security.UserPrincipal;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.common.exception.ApiException;
import com.marketplace.common.util.EnumFormat;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.entity.SellerStatus;
import com.marketplace.seller.repository.SellerProfileRepository;
import com.marketplace.user.entity.Role;
import com.marketplace.user.entity.User;
import com.marketplace.user.service.UserService;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminSellerService {

    private final SellerProfileRepository sellerProfileRepository;
    private final UserService userService;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminSellerService(
            SellerProfileRepository sellerProfileRepository,
            UserService userService,
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.sellerProfileRepository = sellerProfileRepository;
        this.userService = userService;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminSellerResponse> list(SellerStatus status, Pageable pageable) {
        Page<SellerProfile> page = status == null
                ? sellerProfileRepository.findAll(pageable)
                : sellerProfileRepository.findByStatus(status, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public AdminSellerResponse get(UUID sellerId) {
        return toResponse(getSeller(sellerId));
    }

    @Transactional
    public AdminSellerResponse updateStatus(UUID sellerId, AdminSellerActionRequest request) {
        SellerProfile profile = getSeller(sellerId);
        String action = request.action().toLowerCase();

        switch (action) {
            case "approve" -> {
                profile.setStatus(SellerStatus.APPROVED);
                userService.addRole(profile.getUserId(), Role.SELLER);
                writeAudit("SELLER_APPROVED", profile);
            }
            case "reject" -> {
                profile.setStatus(SellerStatus.REJECTED);
                writeAudit("SELLER_REJECTED", profile, request.reason());
            }
            case "suspend" -> {
                profile.setStatus(SellerStatus.SUSPENDED);
                writeAudit("SELLER_SUSPENDED", profile, request.reason());
            }
            case "reactivate" -> {
                profile.setStatus(SellerStatus.APPROVED);
                writeAudit("SELLER_REACTIVATED", profile);
            }
            default -> throw new ApiException("INVALID_ACTION", "Unknown action: " + request.action(), HttpStatus.BAD_REQUEST);
        }

        return toResponse(sellerProfileRepository.save(profile));
    }

    private SellerProfile getSeller(UUID sellerId) {
        return sellerProfileRepository.findById(sellerId)
                .orElseThrow(() -> new ApiException("SELLER_NOT_FOUND", "Seller not found", HttpStatus.NOT_FOUND));
    }

    private AdminSellerResponse toResponse(SellerProfile profile) {
        User user = userService.getById(profile.getUserId());
        return new AdminSellerResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getShopName(),
                profile.getSlug(),
                profile.getDescription(),
                EnumFormat.toApi(profile.getStatus()),
                user.getEmail(),
                user.getFullName(),
                profile.getCreatedAt()
        );
    }

    private void writeAudit(String action, SellerProfile profile) {
        writeAudit(action, profile, null);
    }

    private void writeAudit(String action, SellerProfile profile, String reason) {
        AuditLog log = new AuditLog();
        log.setActorId(currentActorId());
        log.setAction(action);
        log.setTargetType("SellerProfile");
        log.setTargetId(profile.getId());
        try {
            Map<String, Object> metadata = reason == null
                    ? Map.of("shopName", profile.getShopName())
                    : Map.of("shopName", profile.getShopName(), "reason", reason);
            log.setMetadataJson(objectMapper.writeValueAsString(metadata));
        } catch (JsonProcessingException e) {
            log.setMetadataJson("{}");
        }
        auditLogRepository.save(log);
    }

    private static UUID currentActorId() {
        return SecurityUtils.currentUser().map(UserPrincipal::getId).orElse(null);
    }
}
