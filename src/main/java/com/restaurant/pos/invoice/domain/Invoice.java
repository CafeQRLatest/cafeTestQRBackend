package com.restaurant.pos.invoice.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @Column(nullable = false)
    private UUID orderId;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private String paymentStatus; // PAID, UNPAID, PARTIAL
    private String paymentMethod; // CASH, CARD, UPI

    // Useful for offline sync exactly like Order idempotency
    @Column(unique = true)
    private String idempotencyKey;
}
