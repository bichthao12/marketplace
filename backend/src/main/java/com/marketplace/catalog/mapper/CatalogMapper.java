package com.marketplace.catalog.mapper;

import com.marketplace.catalog.dto.CategoryRefResponse;
import com.marketplace.catalog.dto.CategoryResponse;
import com.marketplace.catalog.dto.ProductDetailResponse;
import com.marketplace.catalog.dto.ProductImageResponse;
import com.marketplace.catalog.dto.ProductSummaryResponse;
import com.marketplace.catalog.dto.SellerSummaryResponse;
import com.marketplace.catalog.dto.ShopResponse;
import com.marketplace.catalog.dto.VariantResponse;
import com.marketplace.catalog.entity.Category;
import com.marketplace.catalog.entity.Inventory;
import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductImage;
import com.marketplace.catalog.entity.ProductVariant;
import com.marketplace.seller.entity.SellerProfile;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static CategoryResponse toCategoryTree(Category category, Map<UUID, List<Category>> childrenByParent) {
        List<CategoryResponse> children = childrenByParent.getOrDefault(category.getId(), List.of()).stream()
                .map(child -> toCategoryTree(child, childrenByParent))
                .toList();
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getImageUrl(), children);
    }

    public static SellerSummaryResponse toSellerSummary(SellerProfile seller) {
        return new SellerSummaryResponse(seller.getId(), seller.getShopName(), seller.getSlug());
    }

    public static ProductSummaryResponse toProductSummary(
            Product product,
            String thumbnailUrl,
            BigDecimal priceFrom,
            boolean inStock
    ) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                thumbnailUrl,
                priceFrom,
                product.getCurrency(),
                toSellerSummary(product.getSeller()),
                inStock
        );
    }

    public static ProductDetailResponse toProductDetail(
            Product product,
            List<ProductImage> images,
            List<ProductVariant> variants,
            Map<UUID, Inventory> inventoryByVariant
    ) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getCurrency(),
                product.getStatus(),
                new CategoryRefResponse(product.getCategory().getId(), product.getCategory().getName(), product.getCategory().getSlug()),
                toSellerSummary(product.getSeller()),
                images.stream()
                        .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                        .map(img -> new ProductImageResponse(img.getId(), img.getUrl(), img.getSortOrder()))
                        .toList(),
                variants.stream()
                        .map(variant -> toVariant(variant, inventoryByVariant.get(variant.getId())))
                        .toList()
        );
    }

    public static VariantResponse toVariant(ProductVariant variant, Inventory inventory) {
        int available = inventory != null ? inventory.getAvailableQty() : 0;
        return new VariantResponse(
                variant.getId(),
                variant.getSku(),
                variant.getAttributes(),
                variant.getPrice(),
                variant.getCompareAtPrice(),
                available
        );
    }

    public static ShopResponse toShop(SellerProfile seller, long productCount) {
        return new ShopResponse(
                seller.getId(),
                seller.getShopName(),
                seller.getSlug(),
                seller.getDescription(),
                seller.getLogoUrl(),
                productCount,
                seller.getCreatedAt()
        );
    }
}
