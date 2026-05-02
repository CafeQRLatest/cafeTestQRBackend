package com.restaurant.pos.expense.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.pos.expense.dto.ExpenseResponse;
import com.restaurant.pos.expense.exception.IdempotencyStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis-backed implementation of IdempotencyStore.
 * Ensures FAANG-grade idempotency across multiple server nodes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "idempotency:expense:";
    private static final Duration TTL = Duration.ofHours(24);

    @Override
    public ExpenseResponse get(String key) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + key);
            if (json == null) return null;
            return objectMapper.readValue(json, ExpenseResponse.class);
        } catch (Exception e) {
            log.error("Redis unavailable — cannot verify idempotency | key={}", key, e);
            throw new IdempotencyStoreException("Idempotency store unavailable", e);
        }
    }

    @Override
    public void put(String key, ExpenseResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(PREFIX + key, json, TTL);
        } catch (JsonProcessingException e) {
            // Serialization failure is a programming error — rethrow
            throw new IllegalStateException("Failed to serialize ExpenseResponse for idempotency store", e);
        } catch (Exception e) {
            // Redis failure — log and rethrow so the transaction rolls back
            // A created expense with no idempotency record is worse than a failed request
            log.error("Redis unavailable — idempotency key not stored | key={}", key, e);
            throw new IdempotencyStoreException("Idempotency store unavailable", e);
        }
    }
}
