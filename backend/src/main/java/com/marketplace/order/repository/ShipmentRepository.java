package com.marketplace.order.repository;

import com.marketplace.order.entity.Shipment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderGroupId(UUID orderGroupId);

    List<Shipment> findByOrderGroupIdIn(List<UUID> orderGroupIds);
}
