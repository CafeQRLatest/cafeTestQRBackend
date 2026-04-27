package com.restaurant.pos.expense.repository;

import com.restaurant.pos.expense.domain.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {

    List<ExpenseCategory> findByClientIdAndIsactiveOrderBySortOrderAsc(UUID clientId, String isactive);

    List<ExpenseCategory> findByClientIdOrderBySortOrderAsc(UUID clientId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE ExpenseCategory c SET c.isactive = :status WHERE c.id = :id")
    void updateStatus(@org.springframework.data.repository.query.Param("id") java.util.UUID id, @org.springframework.data.repository.query.Param("status") String status);
}
