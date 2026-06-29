package com.marketplace.catalog.repository;

import com.marketplace.catalog.entity.Inventory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    List<Inventory> findByVariantIdIn(Collection<UUID> variantIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.variantId = :variantId")
    Optional<Inventory> findByVariantIdForUpdate(@Param("variantId") UUID variantId);

    @Modifying
    @Query(value = """
            UPDATE inventory
            SET reserved_qty = reserved_qty + :delta
            WHERE variant_id = :variantId
              AND quantity - reserved_qty >= :delta
            """, nativeQuery = true)
    int incrementReserved(@Param("variantId") UUID variantId, @Param("delta") int delta);

    @Modifying
    @Query(value = """
            UPDATE inventory
            SET reserved_qty = GREATEST(0, reserved_qty + :delta)
            WHERE variant_id = :variantId
            """, nativeQuery = true)
    int adjustReserved(@Param("variantId") UUID variantId, @Param("delta") int delta);
}
