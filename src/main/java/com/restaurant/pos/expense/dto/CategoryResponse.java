package com.restaurant.pos.expense.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Expense category response payload")
public class CategoryResponse {

    @Schema(description = "Unique identifier of the category")
    private UUID id;

    @Schema(description = "Name of the category")
    private String name;

    @Schema(description = "Display sort order")
    private Integer sortOrder;

    @Schema(description = "Whether the category is active", example = "true")
    private Boolean isActive;

    @Schema(description = "When this record was created")
    private LocalDateTime createdAt;

    @Schema(description = "When this record was last updated")
    private LocalDateTime updatedAt;
}
