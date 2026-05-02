package com.restaurant.pos.expense.repository;

import com.restaurant.pos.expense.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Enterprise-grade Repository for Expense entities.
 * Inherits from the unified order infrastructure but provides 
 * a domain-specific interface for financial auditing.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {
    // Expense-specific query methods can be added here
}
