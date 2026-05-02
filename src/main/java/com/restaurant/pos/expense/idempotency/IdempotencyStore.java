package com.restaurant.pos.expense.idempotency;

import com.restaurant.pos.expense.dto.ExpenseResponse;

/**
 * Defines the contract for an idempotency store to prevent duplicate financial transactions.
 */
public interface IdempotencyStore {

    /**
     * Retrieves a previously cached response by its idempotency key.
     *
     * @param key the idempotency key (usually prefixed with orgId)
     * @return the cached ExpenseResponse, or null if not found
     */
    ExpenseResponse get(String key);

    /**
     * Stores a successful response against its idempotency key.
     *
     * @param key      the idempotency key
     * @param response the successful response to cache
     */
    void put(String key, ExpenseResponse response);
}
