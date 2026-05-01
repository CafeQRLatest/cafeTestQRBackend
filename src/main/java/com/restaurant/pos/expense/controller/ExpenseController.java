package com.restaurant.pos.expense.controller;

import com.restaurant.pos.common.security.AdminAccess;
import com.restaurant.pos.common.security.StaffAccess;
import com.restaurant.pos.expense.dto.*;
import com.restaurant.pos.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "APIs for tracking branch expenses and categories")
public class ExpenseController {

    private final ExpenseService expenseService;

    // ── Categories ──────────────────────────────────────────────────────────────

    @GetMapping("/categories")
    @StaffAccess
    @Operation(summary = "Get all expense categories", description = "Retrieves a list of all active and inactive expense categories sorted by sort order.")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        log.info("REST request to get all expense categories for current organization context");
        return ResponseEntity.ok(ApiResponse.success(expenseService.getCategories()));
    }

    @PostMapping("/categories")
    @AdminAccess
    @Operation(summary = "Create an expense category", description = "Creates a new expense category.")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("REST request to create expense category: '{}'", request.getName());
        CategoryResponse response = expenseService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/categories/{id}")
    @AdminAccess
    @Operation(summary = "Update an expense category", description = "Updates an existing expense category.")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("REST request to update expense category ID: '{}' to '{}'", id, request.getName());
        return ResponseEntity.ok(ApiResponse.success(expenseService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    @AdminAccess
    @Operation(summary = "Delete an expense category", description = "Soft-deletes an expense category (marks as inactive).")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        log.info("REST request to soft-delete expense category ID: '{}'", id);
        expenseService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Expenses ────────────────────────────────────────────────────────────────

    @GetMapping
    @StaffAccess
    @Operation(summary = "Get all expenses", description = "Retrieves paginated and filtered expense records with audit-ready details.")
    public ResponseEntity<ApiResponse<Page<ExpenseDto.ExpenseResponse>>> getExpenses(
            @Parameter(description = "Search criteria for filtering expenses") ExpenseSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "orderDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to fetch paginated expenses | criteria: {} | page: {}", criteria, pageable.getPageNumber());
        return ResponseEntity.ok(ApiResponse.success(expenseService.getExpenses(criteria, pageable)));
    }

    @PostMapping
    @StaffAccess
    @Operation(summary = "Create an expense", description = "Records a new expense. Automatically generates linked Invoice and Payment records for auditing.")
    public ResponseEntity<ApiResponse<ExpenseDto.ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseDto.CreateExpenseRequest request) {
        log.info("REST request to record expense | amount: {} | category: '{}'", request.getAmount(), request.getCategoryId());
        ExpenseDto.ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

}
