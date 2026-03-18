package com.restaurant.pos.product.repository;

import com.restaurant.pos.product.domain.ProductUpsell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductUpsellRepository extends JpaRepository<ProductUpsell, UUID> {
    List<ProductUpsell> findByParentProductId(UUID parentProductId);
    void deleteByParentProductId(UUID parentProductId);
}
