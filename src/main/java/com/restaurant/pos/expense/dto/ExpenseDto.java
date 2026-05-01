package com.restaurant.pos.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ExpenseDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateExpenseRequest {
        @NotNull(message = "Category ID is required")
        private UUID categoryId;

        private LocalDateTime expenseDate;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        private BigDecimal amount;

        private String description;
        
        private String paymentMethod;

        private UUID branchId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseResponse {
        private UUID id;
        private String referenceNumber;
        private UUID categoryId;
        private String categoryName;
        private LocalDateTime expenseDate;
        private BigDecimal amount;
        private String description;
        private String paymentMethod;
        private Boolean isActive;
        private UUID orgId;
    }
}
