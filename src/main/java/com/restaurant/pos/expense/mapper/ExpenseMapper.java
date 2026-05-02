package com.restaurant.pos.expense.mapper;

import com.restaurant.pos.expense.domain.Expense;
import com.restaurant.pos.expense.dto.ExpenseResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between Expense entities and their DTOs.
 */
@Component
public class ExpenseMapper {

    public ExpenseResponse toExpenseResponse(Expense expense, String categoryName) {
        
        String method = (expense.getReference() != null && !expense.getReference().isBlank()) 
                ? expense.getReference() 
                : "CASH";

        return ExpenseResponse.builder()
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
