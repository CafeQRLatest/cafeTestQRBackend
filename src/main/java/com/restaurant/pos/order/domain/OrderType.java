package com.restaurant.pos.order.domain;

/**
 * Defines what kind of business transaction this Order represents.
 * All three types share the same orders/invoices/payments tables.
 */
public enum OrderType {
    /** Money flowing IN — customer buys from the restaurant */
    SALE,
    /** Money flowing OUT — restaurant buys from a vendor */
    PURCHASE,
    /** Money flowing OUT — operational/petty-cash expense */
    EXPENSE
}
