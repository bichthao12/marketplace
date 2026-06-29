package com.marketplace.seller.repository;

import com.marketplace.seller.entity.SellerProfile;
import com.marketplace.seller.entity.SellerStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, UUID> {

    Optional<SellerProfile> findByUserId(UUID userId);

    Optional<SellerProfile> findBySlug(String slug);

    boolean existsBySlug(String slug);

    long countByStatus(SellerStatus status);

    Page<SellerProfile> findByStatus(SellerStatus status, Pageable pageable);
}
