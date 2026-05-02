package com.restaurant.pos.expense.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Enterprise-grade DTOs for Expense Transactions.
 * Standardized with immutability patterns, fluent builders, and rich API metadata.
 */
public class ExpenseDto {

    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Payload for recording a new expense transaction")
    public static class CreateExpenseRequest {
        @NotNull(message = "Category ID is required")
        @Schema(description = "ID of the associated expense category")
        private UUID categoryId;

        @Schema(description = "Date and time of the expense occurrence")
        private LocalDateTime expenseDate;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        @Schema(description = "Transaction amount", example = "1500.50")
        private BigDecimal amount;

        @Schema(description = "Optional narrative description", example = "Monthly internet bill")
        private String description;
        
        @Schema(description = "Method of payment used", example = "CASH")
        private String paymentMethod;

        @Schema(description = "Target branch ID for organizational attribution")
        private UUID branchId;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Response payload for an expense transaction")
    public static class ExpenseResponse {
        @Schema(description = "Unique transaction identifier")
        private UUID id;

        @Schema(description = "System-generated reference number", example = "EXP-2024-001")
        private String referenceNumber;

        @Schema(description = "Associated category ID")
        private UUID categoryId;

        @Schema(description = "Associated category name for display", example = "Utilities")
        private String categoryName;

        @Schema(description = "ISO-8601 transaction timestamp")
        private LocalDateTime expenseDate;

        @Schema(description = "Total transaction amount", example = "1200.00")
        private BigDecimal amount;

        @Schema(description = "Transaction narrative or notes")
        private String description;

        @Schema(description = "Payment channel recorded", example = "UPI")
        private String paymentMethod;

        @Schema(description = "Operational status of the record", example = "true")
        private Boolean active;

        @Schema(description = "Organizational branch identifier")
        private UUID orgId;
    }
}
