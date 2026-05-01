package com.restaurant.pos.order.domain;

/**
 * Classifies the payment direction.
 * INBOUND = money received (from Sales).
 * OUTBOUND = money paid out (Purchases and Expenses).
 */
public enum PaymentType {
    /** Money received — linked to SALE orders */
    INBOUND,
    /** Money paid out — linked to PURCHASE or EXPENSE orders */
    OUTBOUND
}
