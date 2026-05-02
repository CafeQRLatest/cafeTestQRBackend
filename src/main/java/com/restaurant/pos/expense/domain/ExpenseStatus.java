package com.restaurant.pos.expense.domain;

/**
 * Standardized status constants for Expense transactions and associated documents.
 * Replaces scattered magic strings to ensure compile-time safety and ledger integrity.
 */
public final class ExpenseStatus {
    
    public static final String VOID      = "VOID";
    public static final String PAID      = "PAID";
    public static final String COMPLETED = "COMPLETED";
    public static final String PENDING   = "PENDING";
    
    // Record level soft-delete flags
    public static final String ACTIVE_FLAG   = "Y";
    public static final String INACTIVE_FLAG = "N";

    private ExpenseStatus() {
        // Prevent instantiation
    }
}
