package com.restaurant.pos.expense.controller;

import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.expense.domain.Expense;
import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ── Categories ──────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ExpenseCategory>> createCategory(@RequestBody ExpenseCategory category) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.createCategory(category)));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ExpenseCategory>> updateCategory(
            @PathVariable UUID id, @RequestBody ExpenseCategory category) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateCategory(id, category)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteCategory(@PathVariable UUID id) {
        expenseService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted"));
    }

    // ── Expenses ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Expense>>> getExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenses(start, end)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Expense>> getExpense(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpense(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Expense>> createExpense(@RequestBody Expense expense) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.createExpense(expense)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Expense>> updateExpense(
            @PathVariable UUID id, @RequestBody Expense expense) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateExpense(id, expense)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteExpense(@PathVariable UUID id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted"));
    }
}
