package com.restaurant.pos.expense.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Request payload for creating an expense category")
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "Name of the category", example = "Kitchen Supplies")
    private String name;

    @Min(value = 0, message = "Sort order must be non-negative")
    @Schema(description = "Display sort order", example = "1")
    private Integer sortOrder = 0;
}
