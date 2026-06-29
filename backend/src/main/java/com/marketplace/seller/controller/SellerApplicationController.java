package com.marketplace.seller.controller;

import com.marketplace.seller.dto.SellerApplicationRequest;
import com.marketplace.seller.dto.SellerApplicationResponse;
import com.marketplace.seller.dto.SellerProfileResponse;
import com.marketplace.seller.dto.UpdateSellerProfileRequest;
import com.marketplace.seller.service.SellerApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerApplicationController {

    private final SellerApplicationService sellerApplicationService;

    public SellerApplicationController(SellerApplicationService sellerApplicationService) {
        this.sellerApplicationService = sellerApplicationService;
    }

    @PostMapping("/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public SellerApplicationResponse apply(@Valid @RequestBody SellerApplicationRequest request) {
        return sellerApplicationService.apply(request);
    }

    @GetMapping("/profile")
    public SellerProfileResponse getProfile() {
        return sellerApplicationService.getProfile();
    }

    @PutMapping("/profile")
    public SellerProfileResponse updateProfile(@Valid @RequestBody UpdateSellerProfileRequest request) {
        return sellerApplicationService.updateProfile(request);
    }
}
