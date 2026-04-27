package com.restaurant.pos.waste.repository;

import com.restaurant.pos.waste.domain.WasteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WasteCategoryRepository extends JpaRepository<WasteCategory, UUID> {
    List<WasteCategory> findByClientIdAndIsactiveOrderBySortOrderAsc(UUID clientId, String isactive);
    List<WasteCategory> findByClientIdOrderBySortOrderAsc(UUID clientId);
}