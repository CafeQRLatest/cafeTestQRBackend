package com.restaurant.pos.expense.mapper;

import com.restaurant.pos.expense.domain.ExpenseCategory;
import com.restaurant.pos.expense.dto.CreateCategoryRequest;
import com.restaurant.pos.expense.dto.UpdateCategoryRequest;
import com.restaurant.pos.expense.dto.CategoryResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between Category entities and their DTOs.
 */
@Component
public class ExpenseMapper {

    public ExpenseCategory toEntity(CreateCategoryRequest request) {
        ExpenseCategory category = new ExpenseCategory();
        category.setName(request.getName() != null ? request.getName().trim() : null);
        category.setSortOrder(request.getSortOrder());
        return category;
    }

    public void updateEntity(ExpenseCategory entity, UpdateCategoryRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName().trim());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
    }

    public CategoryResponse toResponse(ExpenseCategory entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .sortOrder(entity.getSortOrder())
                .active("Y".equalsIgnoreCase(entity.getIsactive()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CategoryResponse.SimpleCategory toSimpleResponse(ExpenseCategory entity) {
        return CategoryResponse.SimpleCategory.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public com.restaurant.pos.expense.dto.ExpenseDto.ExpenseResponse toExpenseResponse(
            com.restaurant.pos.expense.domain.Expense expense, 
            String categoryName) {
        
        String method = (expense.getReference() != null && !expense.getReference().isBlank()) 
                ? expense.getReference() 
                : "CASH";

        return com.restaurant.pos.expense.dto.ExpenseDto.ExpenseResponse.builder()
                .id(expense.getId())
                .referenceNumber(expense.getOrderNo())
                .categoryId(expense.getExpenseCategoryId())
                .categoryName(categoryName)
                .expenseDate(expense.getOrderDate())
                .amount(expense.getGrandTotal())
                .description(expense.getDescription())
                .paymentMethod(method)
                .active("Y".equals(expense.getIsactive()))
                .orgId(expense.getOrgId())
                .build();
    }
}
