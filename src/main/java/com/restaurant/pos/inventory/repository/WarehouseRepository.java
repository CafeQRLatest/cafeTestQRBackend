package com.restaurant.pos.inventory.repository;

import com.restaurant.pos.inventory.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    
    List<Warehouse> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    
    List<Warehouse> findByClientIdAndOrgIdOrderByCreatedAtDesc(UUID clientId, UUID orgId);
    
    Optional<Warehouse> findByIdAndClientId(UUID id, UUID clientId);
    
    Optional<Warehouse> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
}
