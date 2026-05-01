package com.restaurant.pos.expense.repository;

import com.restaurant.pos.expense.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {

    List<ExpenseCategory> findByClientIdAndOrgIdAndIsactiveOrderBySortOrderAsc(UUID clientId, UUID orgId, String isactive);

    List<ExpenseCategory> findByClientIdAndOrgIdOrderBySortOrderAsc(UUID clientId, UUID orgId);

    boolean existsByNameIgnoreCaseAndClientIdAndOrgId(String name, UUID clientId, UUID orgId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ExpenseCategory c SET c.isactive = :status WHERE c.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") String status);
}
