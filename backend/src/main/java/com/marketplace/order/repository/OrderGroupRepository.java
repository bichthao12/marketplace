package com.marketplace.order.repository;

import com.marketplace.order.entity.OrderGroup;
import com.marketplace.order.entity.OrderGroupStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderGroupRepository extends JpaRepository<OrderGroup, UUID> {

    List<OrderGroup> findByOrderId(UUID orderId);

    Page<OrderGroup> findBySellerIdOrderByCreatedAtDesc(UUID sellerId, Pageable pageable);

    Page<OrderGroup> findBySellerIdAndStatusOrderByCreatedAtDesc(UUID sellerId, OrderGroupStatus status, Pageable pageable);

    Optional<OrderGroup> findByIdAndSellerId(UUID id, UUID sellerId);

    long countBySellerIdAndStatus(UUID sellerId, OrderGroupStatus status);
}
