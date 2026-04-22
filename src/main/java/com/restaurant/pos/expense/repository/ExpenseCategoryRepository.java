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
}
