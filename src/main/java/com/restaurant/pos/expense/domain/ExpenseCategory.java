package com.restaurant.pos.expense.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "expense_categories")
public class ExpenseCategory extends BaseEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active", length = 1)
    private String isactive = "Y";

    public ExpenseCategory() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    @JsonProperty("isActive")
    public String getIsActive() {
        return isactive;
    }

    @JsonProperty("isActive")
    public void setIsActive(String isactive) {
        this.isactive = isactive;
    }
}
