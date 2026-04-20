package com.restaurant.pos.inventory.domain;

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
@Table(name = "stock_ledgers")
public class StockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Builder.Default
    private UUID id = null;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // PURCHASE, SALE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT

    @Column(name = "reference_id")
    private UUID referenceId; // order ID, transfer ID, etc

    @Column(name = "quantity_change", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantityChange;

    @Column(name = "balance_after_transaction", nullable = false, precision = 15, scale = 3)
    private BigDecimal balanceAfterTransaction;

    @Builder.Default
    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();

    @Column(name = "created_by")
    private UUID createdBy;
}
