package com.restaurant.pos.expense.controller;

import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.common.security.AdminAccess;
import com.restaurant.pos.common.security.StaffAccess;
import com.restaurant.pos.expense.dto.CategoryResponse;
import com.restaurant.pos.expense.dto.CreateCategoryRequest;
import com.restaurant.pos.expense.dto.ExpenseDto;
import com.restaurant.pos.expense.dto.ExpenseSearchCriteria;
import com.restaurant.pos.expense.dto.UpdateCategoryRequest;
import com.restaurant.pos.expense.service.CategoryService;
import com.restaurant.pos.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(
        name = "Expense Management",
        description = "APIs for expense categories and expense transaction management"
)
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    /*
     * ─────────────────────────────────────────────────────────────
     * Expense Categories
     * ─────────────────────────────────────────────────────────────
     */

    @GetMapping("/categories")
    @StaffAccess
    @Operation(
            summary = "Fetch expense categories",
            description = "Returns all expense categories available for the current organization, including active/inactive records ordered by sort priority."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {

        log.info("Fetching expense categories for current organization");

        List<CategoryResponse> response = categoryService.getCategories();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping("/categories")
    @AdminAccess
    @Operation(
            summary = "Create expense category",
            description = "Creates a new expense category for expense classification and reporting."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        log.info(
                "Creating expense category | name={} | sortOrder={}",
                request.getName(),
                request.getSortOrder()
        );

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(response)
                );
    }

    @PutMapping("/categories/{id}")
    @AdminAccess
    @Operation(
            summary = "Update expense category",
            description = "Updates an existing expense category."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        log.info(
                "Updating expense category | categoryId={} | newName={}",
                id,
                request.getName()
        );

        CategoryResponse response = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @DeleteMapping("/categories/{id}")
    @AdminAccess
    @Operation(
            summary = "Delete expense category",
            description = "Soft deletes an expense category by marking it inactive."
    )
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID id
    ) {

        log.info(
                "Soft deleting expense category | categoryId={}",
                id
        );

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success(null)
        );
    }

    /*
     * ─────────────────────────────────────────────────────────────
     * Expenses
     * ─────────────────────────────────────────────────────────────
     */

    @GetMapping
    @StaffAccess
    @Operation(
            summary = "Fetch expenses",
            description = "Returns paginated expense records with filtering support for audit, reporting, and operational review."
    )
    public ResponseEntity<ApiResponse<Page<ExpenseDto.ExpenseResponse>>> getExpenses(
            @Parameter(
                    description = "Search criteria for filtering expense records"
            )
            ExpenseSearchCriteria criteria,

            @PageableDefault(
                    size = 20,
                    sort = "orderDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        log.info(
                "Fetching expenses | filters={} | page={} | size={}",
                criteria,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<ExpenseDto.ExpenseResponse> response =
                expenseService.getExpenses(criteria, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping
    @StaffAccess
    @Operation(
            summary = "Create expense",
            description = "Creates a new expense transaction and automatically generates linked invoice/payment records for financial audit compliance."
    )
    public ResponseEntity<ApiResponse<ExpenseDto.ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseDto.CreateExpenseRequest request
    ) {

        log.info(
                "Creating expense | branchId={} | categoryId={} | amount={}",
                request.getBranchId(),
                request.getCategoryId(),
                request.getAmount()
        );

        ExpenseDto.ExpenseResponse response =
                expenseService.createExpense(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(response)
                );
    }
}