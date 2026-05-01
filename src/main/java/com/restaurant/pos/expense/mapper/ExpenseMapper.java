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
                .isActive("Y".equalsIgnoreCase(entity.getIsactive()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
