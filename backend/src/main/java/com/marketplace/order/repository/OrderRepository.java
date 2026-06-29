package com.marketplace.order.repository;

import com.marketplace.order.entity.Order;
import com.marketplace.order.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(UUID buyerId, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndBuyerId(UUID id, UUID buyerId);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant before);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);
}
