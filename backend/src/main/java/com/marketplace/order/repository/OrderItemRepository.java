package com.marketplace.order.repository;

import com.marketplace.order.entity.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderGroupId(UUID orderGroupId);

    List<OrderItem> findByOrderGroupIdIn(List<UUID> orderGroupIds);
}
