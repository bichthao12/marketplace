package com.marketplace.catalog.repository;

import com.marketplace.catalog.entity.ProductVariant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductIdOrderByCreatedAtAsc(UUID productId);

    Optional<ProductVariant> findByIdAndProductId(UUID id, UUID productId);

    boolean existsByProductIdAndSku(UUID productId, String sku);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p JOIN FETCH p.seller WHERE v.id IN :ids")
    List<ProductVariant> findByIdInWithProduct(@Param("ids") Collection<UUID> ids);
}
