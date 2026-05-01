package com.restaurant.pos.order.domain;

import com.restaurant.pos.common.entity.BaseEntity;
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
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20)
    private PaymentType paymentType; // INBOUND (Sales) or OUTBOUND (Purchase/Expense)

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "org_id")
    private UUID orgId;

    @Column(name = "terminal_id")
    private UUID terminalId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "expense_category_id")
    private UUID expenseCategoryId;

    @Column(name = "payment_date")
    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(name = "payment_method", length = 50, nullable = false)
    private String paymentMethod;

    @Column(name = "amount_paid", precision = 15, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    @Column(name = "change_given", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Builder.Default
    @Column(name = "isactive", length = 1)
    private String isactive = "Y";
}
