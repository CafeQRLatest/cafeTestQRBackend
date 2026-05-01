package com.restaurant.pos.sequence.domain;

/**
 * Defines the types of documents that can have custom numbering sequences.
 */
public enum DocumentType {
    SALE_ORDER,
    PURCHASE_ORDER,
    EXPENSE,
    CUSTOMER_INVOICE,
    VENDOR_BILL,
    EXPENSE_RECEIPT,
    INBOUND_PAYMENT,
    OUTBOUND_PAYMENT
}
