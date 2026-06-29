package com.marketplace.catalog.service;

import com.marketplace.catalog.dto.ProductDetailResponse;
import com.marketplace.catalog.dto.ProductSummaryResponse;
import com.marketplace.catalog.entity.Inventory;
import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductImage;
import com.marketplace.catalog.entity.ProductStatus;
import com.marketplace.catalog.entity.ProductVariant;
import com.marketplace.catalog.exception.ProductNotFoundException;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.InventoryRepository;
import com.marketplace.catalog.repository.ProductImageRepository;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.repository.ProductVariantRepository;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.seller.entity.SellerStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final InventoryRepository inventoryRepository;

    public ProductCatalogService(
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            InventoryRepository inventoryRepository
    ) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> search(
            String q,
            String categorySlug,
            String sellerSlug,
            String currency,
            Pageable pageable
    ) {
        Page<Product> page = productRepository.searchPublic(
                blankToNull(q),
                blankToNull(categorySlug),
                blankToNull(sellerSlug),
                blankToNull(currency),
                pageable
        );
        return PageResponse.from(page.map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findBySlugWithDetails(slug)
                .filter(this::isPubliclyVisible)
                .orElseThrow(ProductNotFoundException::new);

        List<ProductVariant> variants = variantRepository.findByProductIdOrderByCreatedAtAsc(product.getId());
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        Map<UUID, Inventory> inventoryByVariant = loadInventory(variants);

        return CatalogMapper.toProductDetail(product, images, variants, inventoryByVariant);
    }

    private ProductSummaryResponse toSummary(Product product) {
        List<ProductVariant> variants = variantRepository.findByProductIdOrderByCreatedAtAsc(product.getId());
        Map<UUID, Inventory> inventoryByVariant = loadInventory(variants);
        BigDecimal priceFrom = variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        boolean inStock = variants.stream()
                .anyMatch(variant -> {
                    Inventory inventory = inventoryByVariant.get(variant.getId());
                    return inventory != null && inventory.getAvailableQty() > 0;
                });
        String thumbnail = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(null);
        return CatalogMapper.toProductSummary(product, thumbnail, priceFrom, inStock);
    }

    private Map<UUID, Inventory> loadInventory(List<ProductVariant> variants) {
        List<UUID> ids = variants.stream().map(ProductVariant::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return inventoryRepository.findByVariantIdIn(ids).stream()
                .collect(Collectors.toMap(Inventory::getVariantId, Function.identity()));
    }

    private boolean isPubliclyVisible(Product product) {
        return product.getStatus() == ProductStatus.ACTIVE
                && product.getSeller().getStatus() == SellerStatus.APPROVED;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
