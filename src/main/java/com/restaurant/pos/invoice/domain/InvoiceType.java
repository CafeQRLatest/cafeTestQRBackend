package com.restaurant.pos.invoice.domain;

/**
 * Classifies the invoice by the direction of the transaction.
 * Auto-set by OrderService based on the parent Order's OrderType.
 */
public enum InvoiceType {
    /** Generated from a SALE order — issued to a customer */
    CUSTOMER_INVOICE,
    /** Generated from a PURCHASE order — received from a vendor */
    VENDOR_BILL,
    /** Generated from an EXPENSE order — petty-cash receipt */
    EXPENSE_RECEIPT
}
