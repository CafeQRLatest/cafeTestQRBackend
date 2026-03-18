package com.restaurant.pos.product.repository;

import com.restaurant.pos.product.domain.ProductVariantMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantMappingRepository extends JpaRepository<ProductVariantMapping, UUID> {
    List<ProductVariantMapping> findByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}
