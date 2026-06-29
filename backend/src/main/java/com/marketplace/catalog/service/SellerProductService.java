package com.marketplace.catalog.service;

import com.marketplace.catalog.dto.CreateProductRequest;
import com.marketplace.catalog.dto.ProductDetailResponse;
import com.marketplace.catalog.dto.ProductImageResponse;
import com.marketplace.catalog.dto.UpdateProductRequest;
import com.marketplace.catalog.dto.UpdateProductStatusRequest;
import com.marketplace.catalog.dto.UpdateVariantRequest;
import com.marketplace.catalog.entity.Category;
import com.marketplace.catalog.entity.Inventory;
import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductImage;
import com.marketplace.catalog.entity.ProductStatus;
import com.marketplace.catalog.entity.ProductVariant;
import com.marketplace.catalog.exception.CategoryNotFoundException;
import com.marketplace.catalog.exception.ProductIncompleteException;
import com.marketplace.catalog.exception.ProductNotFoundException;
import com.marketplace.catalog.mapper.CatalogMapper;
import com.marketplace.catalog.repository.CategoryRepository;
import com.marketplace.catalog.repository.InventoryRepository;
import com.marketplace.catalog.repository.ProductImageRepository;
import com.marketplace.catalog.repository.ProductRepository;
import com.marketplace.catalog.repository.ProductVariantRepository;
import com.marketplace.catalog.storage.ImageStorageService;
import com.marketplace.catalog.util.SlugUtils;
import com.marketplace.common.dto.PageResponse;
import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.service.SellerContextService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SellerProductService {

    private final SellerContextService sellerContextService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository imageRepository;
    private final ImageStorageService imageStorageService;

    public SellerProductService(
            SellerContextService sellerContextService,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductVariantRepository variantRepository,
            InventoryRepository inventoryRepository,
            ProductImageRepository imageRepository,
            ImageStorageService imageStorageService
    ) {
        this.sellerContextService = sellerContextService;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.imageRepository = imageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDetailResponse> list(ProductStatus status, Pageable pageable) {
        SellerProfile seller = sellerContextService.findCurrentSellerProfile();
        Page<Product> page = status == null
                ? productRepository.findBySellerId(seller.getId(), pageable)
                : productRepository.findBySellerIdAndStatus(seller.getId(), status, pageable);
        return PageResponse.from(page.map(this::toDetail));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse get(UUID productId) {
        Product product = requireOwnedProduct(productId);
        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse create(CreateProductRequest request) {
        SellerProfile seller = sellerContextService.requireCurrentSellerProfile();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(CategoryNotFoundException::new);

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setName(request.name().trim());
        product.setSlug(SlugUtils.uniqueSlug(request.name(), productRepository::existsBySlug));
        product.setDescription(request.description());
        product.setCurrency(seller.getCurrency());
        product.setStatus(ProductStatus.DRAFT);
        product = productRepository.save(product);

        for (CreateProductRequest.CreateVariantRequest variantRequest : request.variants()) {
            if (variantRepository.existsByProductIdAndSku(product.getId(), variantRequest.sku())) {
                throw new ProductIncompleteException("Duplicate SKU: " + variantRequest.sku());
            }
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(variantRequest.sku().trim());
            variant.setPrice(variantRequest.price());
            variant.setCompareAtPrice(variantRequest.compareAtPrice());
            variant.setAttributes(variantRequest.attributes());
            variant = variantRepository.save(variant);

            Inventory inventory = new Inventory();
            inventory.setVariantId(variant.getId());
            inventory.setQuantity(variantRequest.quantity());
            inventory.setReservedQty(0);
            inventoryRepository.save(inventory);
        }

        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse update(UUID productId, UpdateProductRequest request) {
        Product product = requireOwnedProduct(productId);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(CategoryNotFoundException::new);
            product.setCategory(category);
        }
        productRepository.save(product);
        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse updateStatus(UUID productId, UpdateProductStatusRequest request) {
        Product product = requireOwnedProduct(productId);
        if (request.status() == ProductStatus.ACTIVE) {
            validatePublishable(product);
        }
        product.setStatus(request.status());
        productRepository.save(product);
        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse updateVariant(UUID productId, UUID variantId, UpdateVariantRequest request) {
        requireOwnedProduct(productId);
        ProductVariant variant = variantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(ProductNotFoundException::new);
        variant.setPrice(request.price());
        if (request.attributes() != null) {
            variant.setAttributes(request.attributes());
        }
        variantRepository.save(variant);

        Inventory inventory = inventoryRepository.findById(variantId).orElseGet(() -> {
            Inventory created = new Inventory();
            created.setVariantId(variantId);
            created.setReservedQty(0);
            return created;
        });
        inventory.setQuantity(request.quantity());
        inventoryRepository.save(inventory);

        return get(productId);
    }

    @Transactional
    public ProductImageResponse uploadImage(UUID productId, MultipartFile file) throws IOException {
        Product product = requireOwnedProduct(productId);
        String url = imageStorageService.storeProductImage(
                product.getSeller().getId().toString(),
                product.getId().toString(),
                file
        );
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setSortOrder((int) imageRepository.countByProductId(productId));
        image = imageRepository.save(image);
        return new ProductImageResponse(image.getId(), image.getUrl(), image.getSortOrder());
    }

    @Transactional
    public void deleteImage(UUID productId, UUID imageId) throws IOException {
        requireOwnedProduct(productId);
        ProductImage image = imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(ProductNotFoundException::new);
        imageStorageService.delete(image.getUrl());
        imageRepository.delete(image);
    }

    private Product requireOwnedProduct(UUID productId) {
        SellerProfile seller = sellerContextService.findCurrentSellerProfile();
        return productRepository.findByIdAndSellerId(productId, seller.getId())
                .orElseThrow(ProductNotFoundException::new);
    }

    private void validatePublishable(Product product) {
        long variantCount = variantRepository.findByProductIdOrderByCreatedAtAsc(product.getId()).size();
        long imageCount = imageRepository.countByProductId(product.getId());
        if (variantCount == 0) {
            throw new ProductIncompleteException("Product must have at least one variant");
        }
        if (imageCount == 0) {
            throw new ProductIncompleteException("Product must have at least one image");
        }
    }

    private ProductDetailResponse toDetail(Product product) {
        List<ProductVariant> variants = variantRepository.findByProductIdOrderByCreatedAtAsc(product.getId());
        List<ProductImage> images = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        Map<UUID, Inventory> inventoryByVariant = inventoryRepository.findByVariantIdIn(
                variants.stream().map(ProductVariant::getId).toList()
        ).stream().collect(Collectors.toMap(Inventory::getVariantId, Function.identity()));
        return CatalogMapper.toProductDetail(product, images, variants, inventoryByVariant);
    }
}
