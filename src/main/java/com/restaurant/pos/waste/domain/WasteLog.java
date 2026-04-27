package com.restaurant.pos.waste.domain;

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
@Table(name = "waste_logs")
public class WasteLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "waste_category_id")
    private UUID wasteCategoryId;

    @Transient
    private String categoryName;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "waste_reason", nullable = false, length = 50)
    private String wasteReason;

    @Column(nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_of_measure", length = 20)
    @Builder.Default
    private String unitOfMeasure = "units";

    @Column(name = "unit_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "total_cost", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "waste_date")
    @Builder.Default
    private LocalDateTime wasteDate = LocalDateTime.now();

    @Builder.Default
    @JsonProperty("isActive")
    @Column(name = "isactive", length = 1)
    private String isactive = "Y";
}