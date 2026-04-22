package com.restaurant.pos.expense.repository;

import com.restaurant.pos.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    List<Expense> findByClientIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            UUID clientId, LocalDateTime start, LocalDateTime end);

    List<Expense> findByClientIdOrderByExpenseDateDesc(UUID clientId);

    long countByCategoryId(UUID categoryId);
}
