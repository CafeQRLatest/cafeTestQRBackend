package com.restaurant.pos.expense.exception;

import com.restaurant.pos.common.exception.BusinessException;

/**
 * Exception thrown when attempting to modify or process an expense
 * that has already been marked as VOID or inactive.
 */
public class ExpenseAlreadyVoidedException extends BusinessException {

    public ExpenseAlreadyVoidedException(String message) {
        super(message);
    }
    
    public ExpenseAlreadyVoidedException() {
        super("Cannot modify a voided or inactive expense record");
    }
}
