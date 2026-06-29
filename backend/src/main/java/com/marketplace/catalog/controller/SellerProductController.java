package com.marketplace.catalog.controller;

import com.marketplace.catalog.dto.CreateProductRequest;
import com.marketplace.catalog.dto.ProductDetailResponse;
import com.marketplace.catalog.dto.ProductImageResponse;
import com.marketplace.catalog.dto.UpdateProductRequest;
import com.marketplace.catalog.dto.UpdateProductStatusRequest;
import com.marketplace.catalog.dto.UpdateVariantRequest;
import com.marketplace.catalog.entity.ProductStatus;
import com.marketplace.catalog.service.SellerProductService;
import com.marketplace.common.dto.PageResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/seller/products")
public class SellerProductController {

    private final SellerProductService sellerProductService;

    public SellerProductController(SellerProductService sellerProductService) {
        this.sellerProductService = sellerProductService;
    }

    @GetMapping
    public PageResponse<ProductDetailResponse> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        return sellerProductService.list(status, pageable);
    }

    @GetMapping("/{productId}")
    public ProductDetailResponse get(@PathVariable UUID productId) {
        return sellerProductService.get(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDetailResponse create(@Valid @RequestBody CreateProductRequest request) {
        return sellerProductService.create(request);
    }

    @PutMapping("/{productId}")
    public ProductDetailResponse update(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return sellerProductService.update(productId, request);
    }

    @PatchMapping("/{productId}/status")
    public ProductDetailResponse updateStatus(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        return sellerProductService.updateStatus(productId, request);
    }

    @PutMapping("/{productId}/variants/{variantId}")
    public ProductDetailResponse updateVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateVariantRequest request
    ) {
        return sellerProductService.updateVariant(productId, variantId, request);
    }

    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse uploadImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return sellerProductService.uploadImage(productId, file);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable UUID productId, @PathVariable UUID imageId) throws IOException {
        sellerProductService.deleteImage(productId, imageId);
    }
}
