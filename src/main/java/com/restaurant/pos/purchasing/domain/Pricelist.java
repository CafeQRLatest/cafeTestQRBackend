package com.restaurant.pos.purchasing.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "pricelists")
public class Pricelist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Builder.Default
    private UUID id = null;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "pricelist_type", length = 20, nullable = false)
    private String pricelistType; // SALE, PURCHASE

    @Column(length = 150, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Builder.Default
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "markup_percentage", precision = 5, scale = 2)
    private BigDecimal markupPercentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_default")
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "pricelist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PricelistVersion> versions = new ArrayList<>();

    @Builder.Default
    @JsonProperty("isActive")
    @Column(name = "isactive", length = 1)
    private String isactive = "Y";
}
