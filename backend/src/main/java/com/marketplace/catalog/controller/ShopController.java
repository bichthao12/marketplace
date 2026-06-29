package com.marketplace.catalog.controller;

import com.marketplace.catalog.dto.ShopResponse;
import com.marketplace.catalog.service.ShopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/{slug}")
    public ShopResponse getBySlug(@PathVariable String slug) {
        return shopService.getBySlug(slug);
    }
}
