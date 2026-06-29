package com.marketplace.admin.controller;

import com.marketplace.admin.dto.AdminSellerActionRequest;
import com.marketplace.admin.dto.AdminSellerResponse;
import com.marketplace.admin.dto.AuditLogResponse;
import com.marketplace.admin.service.AdminSellerService;
import com.marketplace.admin.service.AuditLogService;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.seller.entity.SellerStatus;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/sellers")
public class AdminSellerController {

    private final AdminSellerService adminSellerService;

    public AdminSellerController(AdminSellerService adminSellerService) {
        this.adminSellerService = adminSellerService;
    }

    @GetMapping
    public PageResponse<AdminSellerResponse> list(
            @RequestParam(required = false) SellerStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return adminSellerService.list(status, pageable);
    }

    @GetMapping("/{sellerId}")
    public AdminSellerResponse get(@PathVariable UUID sellerId) {
        return adminSellerService.get(sellerId);
    }

    @PatchMapping("/{sellerId}")
    public AdminSellerResponse update(
            @PathVariable UUID sellerId,
            @Valid @RequestBody AdminSellerActionRequest request
    ) {
        return adminSellerService.updateStatus(sellerId, request);
    }
}
