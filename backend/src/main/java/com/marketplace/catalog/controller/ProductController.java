package com.marketplace.catalog.controller;

import com.marketplace.catalog.dto.ProductDetailResponse;
import com.marketplace.catalog.dto.ProductSummaryResponse;
import com.marketplace.catalog.service.ProductCatalogService;
import com.marketplace.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping
    public PageResponse<ProductSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String sellerSlug,
            @RequestParam(required = false) String currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), parseSort(sort));
        return productCatalogService.search(q, categorySlug, sellerSlug, currency, pageable);
    }

    @GetMapping("/{slug}")
    public ProductDetailResponse getBySlug(@PathVariable String slug) {
        return productCatalogService.getBySlug(slug);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String property = switch (parts[0]) {
            case "price" -> "createdAt"; // price sort needs join — MVP fallback
            case "name" -> "name";
            default -> "createdAt";
        };
        return Sort.by(direction, property);
    }
}
