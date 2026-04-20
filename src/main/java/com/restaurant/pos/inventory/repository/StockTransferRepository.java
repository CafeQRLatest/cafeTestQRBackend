package com.restaurant.pos.inventory.repository;

import com.restaurant.pos.inventory.domain.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {
    
    List<StockTransfer> findByClientIdOrderByTransferDateDesc(UUID clientId);
    
    List<StockTransfer> findByClientIdAndOrgIdOrderByTransferDateDesc(UUID clientId, UUID orgId);
    
    Optional<StockTransfer> findByIdAndClientId(UUID id, UUID clientId);
    
    Optional<StockTransfer> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
}
