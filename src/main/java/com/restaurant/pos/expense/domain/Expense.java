package com.restaurant.pos.expense.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "expenses")
public class Expense extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "expense_date", nullable = false)
    @Builder.Default
    private LocalDateTime expenseDate = LocalDateTime.now();

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Builder.Default
    @JsonProperty("isActive")
    @Column(name = "is_active", length = 1)
    private String isactive = "Y";

    // Transient field for category name (populated by service)
    @Transient
    private String categoryName;
}
