package com.restaurant.pos.purchasing.repository;

import com.restaurant.pos.purchasing.domain.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {
    List<Vendor> findByClientIdOrderByNameAsc(UUID clientId);
    List<Vendor> findByClientIdAndOrgIdOrderByNameAsc(UUID clientId, UUID orgId);
    Optional<Vendor> findByIdAndClientId(UUID id, UUID clientId);
    Optional<Vendor> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
}
