package com.restaurant.pos.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantGroupDto {
    private UUID id;
    private String name;

    @JsonProperty("isActive")
    private boolean isActive;

    @Builder.Default
    private List<VariantOptionDto> options = new ArrayList<>();
}
