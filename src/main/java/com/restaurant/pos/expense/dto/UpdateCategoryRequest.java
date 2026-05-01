package com.restaurant.pos.expense.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request payload for updating an expense category")
public class UpdateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "Name of the category")
    private String name;

    @Min(value = 0, message = "Sort order must be non-negative")
    @Schema(description = "Display sort order")
    private Integer sortOrder;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive;
}
