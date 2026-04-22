package com.restaurant.pos.expense.service;

import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.expense.domain.Expense;
import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.repository.ExpenseCategoryRepository;
import com.restaurant.pos.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;

    // ── Categories ──────────────────────────────────────────────────────────────

    public List<ExpenseCategory> getCategories() {
        UUID clientId = TenantContext.getCurrentTenant();
        return categoryRepository.findByClientIdAndIsactiveOrderBySortOrderAsc(clientId, "Y");
    }

    @Transactional
    public ExpenseCategory createCategory(ExpenseCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public ExpenseCategory updateCategory(UUID id, ExpenseCategory updated) {
        ExpenseCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        cat.setName(updated.getName());
        if (updated.getSortOrder() != null) cat.setSortOrder(updated.getSortOrder());
        return categoryRepository.save(cat);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        long count = expenseRepository.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("Cannot delete: category is used by " + count + " expense(s)");
        }
        categoryRepository.deleteById(id);
    }

    // ── Expenses ────────────────────────────────────────────────────────────────

    public List<Expense> getExpenses(LocalDateTime start, LocalDateTime end) {
        UUID clientId = TenantContext.getCurrentTenant();
        List<Expense> expenses;
        if (start != null && end != null) {
            expenses = expenseRepository.findByClientIdAndExpenseDateBetweenOrderByExpenseDateDesc(clientId, start, end);
        } else {
            expenses = expenseRepository.findByClientIdOrderByExpenseDateDesc(clientId);
        }

        // Populate category names
        Map<UUID, String> catMap = categoryRepository.findByClientIdOrderBySortOrderAsc(clientId)
                .stream()
                .collect(Collectors.toMap(ExpenseCategory::getId, ExpenseCategory::getName, (a, b) -> a));

        expenses.forEach(e -> {
            if (e.getCategoryId() != null) {
                e.setCategoryName(catMap.getOrDefault(e.getCategoryId(), "Uncategorized"));
            }
        });

        return expenses;
    }

    public Expense getExpense(UUID id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Transactional
    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(UUID id, Expense updated) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        expense.setAmount(updated.getAmount());
        expense.setCategoryId(updated.getCategoryId());
        expense.setExpenseDate(updated.getExpenseDate());
        expense.setDescription(updated.getDescription());
        expense.setPaymentMethod(updated.getPaymentMethod());
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(UUID id) {
        expenseRepository.deleteById(id);
    }
}
