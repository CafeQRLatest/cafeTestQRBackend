package com.restaurant.pos.inventory.repository;

import com.restaurant.pos.inventory.domain.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, UUID> {
    
    List<StockAdjustment> findByClientIdOrderByAdjustmentDateDesc(UUID clientId);
    
    List<StockAdjustment> findByClientIdAndOrgIdOrderByAdjustmentDateDesc(UUID clientId, UUID orgId);
    
    Optional<StockAdjustment> findByIdAndClientId(UUID id, UUID clientId);
    
    Optional<StockAdjustment> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
}
