package com.restaurant.pos.expense.domain;

import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Enterprise-grade Domain Entity for Expenses.
 * Uses Single Table Inheritance to map Expense-specific logic 
 * onto the unified 'orders' table.
 */
@Entity
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@DiscriminatorValue("EXPENSE")
public class Expense extends Order {

    /**
     * Custom initialization for Expense domain.
     */
    public Expense(ExpenseCategory category) {
        super();
        this.setOrderType(OrderType.EXPENSE);
        this.setOrderStatus("COMPLETED");
        this.setPaymentStatus("PAID");
        if (category != null) {
            this.setExpenseCategoryId(category.getId());
        }
    }
    
    // You can add expense-specific domain logic here in the future
    // e.g. public boolean isTaxDeductible() { ... }
}
