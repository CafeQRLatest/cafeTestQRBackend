package com.restaurant.pos.expense.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(
    name = "expense_categories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_expense_cat_name", columnNames = {"client_id", "org_id", "name"})
    },
    indexes = {
        @Index(name = "idx_expense_cat_client", columnList = "client_id"),
        @Index(name = "idx_expense_cat_org", columnList = "org_id"),
        @Index(name = "idx_expense_cat_active", columnList = "is_active"),
        @Index(name = "idx_expense_cat_sort", columnList = "sort_order")
    }
)
public class ExpenseCategory extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Builder.Default
    @JsonProperty("isActive")
    @Column(name = "is_active", nullable = false, length = 1)
    private String isactive = "Y";
}
