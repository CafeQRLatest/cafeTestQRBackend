package com.restaurant.pos.purchasing.repository;

import com.restaurant.pos.purchasing.domain.Pricelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PricelistRepository extends JpaRepository<Pricelist, UUID> {
    List<Pricelist> findByClientIdOrderByNameAsc(UUID clientId);
    List<Pricelist> findByClientIdAndOrgIdOrderByNameAsc(UUID clientId, UUID orgId);
    List<Pricelist> findByClientIdAndPricelistTypeOrderByNameAsc(UUID clientId, String pricelistType);
    Optional<Pricelist> findByIdAndClientId(UUID id, UUID clientId);
    List<Pricelist> findByClientIdAndPricelistTypeAndIsDefaultTrue(UUID clientId, String pricelistType);
}
