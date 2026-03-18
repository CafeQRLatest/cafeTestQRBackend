package com.restaurant.pos.product.repository;

import com.restaurant.pos.product.domain.VariantPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VariantPricingRepository extends JpaRepository<VariantPricing, UUID> {
    List<VariantPricing> findByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}
