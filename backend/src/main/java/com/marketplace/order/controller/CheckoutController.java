package com.marketplace.order.controller;

import com.marketplace.order.dto.CheckoutPreviewRequest;
import com.marketplace.order.dto.CheckoutPreviewResponse;
import com.marketplace.order.dto.CheckoutRequest;
import com.marketplace.order.dto.CheckoutResponse;
import com.marketplace.order.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/preview")
    public CheckoutPreviewResponse preview(@Valid @RequestBody CheckoutPreviewRequest request) {
        return checkoutService.preview(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }
}
