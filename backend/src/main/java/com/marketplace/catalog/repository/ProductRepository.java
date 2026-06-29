package com.marketplace.catalog.repository;

import com.marketplace.catalog.entity.Product;
import com.marketplace.catalog.entity.ProductStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySlug(String slug);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category WHERE p.slug = :slug")
    Optional<Product> findBySlugWithDetails(@Param("slug") String slug);

    @Query("SELECT p FROM Product p JOIN FETCH p.seller JOIN FETCH p.category WHERE p.id = :id AND p.seller.id = :sellerId")
    Optional<Product> findByIdAndSellerId(@Param("id") UUID id, @Param("sellerId") UUID sellerId);

    boolean existsBySlug(String slug);

    Page<Product> findBySellerIdAndStatus(UUID sellerId, ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(UUID sellerId, Pageable pageable);

    long countBySellerId(UUID sellerId);

    long countBySellerIdAndStatus(UUID sellerId, ProductStatus status);

    @Query(value = """
            SELECT p.* FROM products p
            JOIN seller_profiles s ON s.id = p.seller_id
            LEFT JOIN categories c ON c.id = p.category_id
            WHERE p.status = 'ACTIVE'
              AND s.status = 'APPROVED'
              AND (:categorySlug IS NULL OR c.slug = :categorySlug)
              AND (:sellerSlug IS NULL OR s.slug = :sellerSlug)
              AND (:currency IS NULL OR p.currency = :currency)
              AND (:q IS NULL OR p.search_vector @@ plainto_tsquery('simple', :q)
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY p.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            JOIN seller_profiles s ON s.id = p.seller_id
            LEFT JOIN categories c ON c.id = p.category_id
            WHERE p.status = 'ACTIVE'
              AND s.status = 'APPROVED'
              AND (:categorySlug IS NULL OR c.slug = :categorySlug)
              AND (:sellerSlug IS NULL OR s.slug = :sellerSlug)
              AND (:currency IS NULL OR p.currency = :currency)
              AND (:q IS NULL OR p.search_vector @@ plainto_tsquery('simple', :q)
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            nativeQuery = true)
    Page<Product> searchPublic(
            @Param("q") String q,
            @Param("categorySlug") String categorySlug,
            @Param("sellerSlug") String sellerSlug,
            @Param("currency") String currency,
            Pageable pageable
    );
}
