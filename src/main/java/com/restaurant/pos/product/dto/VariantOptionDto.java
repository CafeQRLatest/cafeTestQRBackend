package com.restaurant.pos.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantOptionDto {
    private UUID id;
    private UUID groupId;
    private String name;
    private BigDecimal additionalPrice;

    @JsonProperty("isActive")
    private boolean isActive;
}
