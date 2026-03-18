package com.restaurant.pos.order.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    // The tablet/client can send this idempotency key during offline sync
    @Column(unique = true)
    private String idempotencyKey;

    private String customerName;
    private String customerPhone;
    
    // UUID from the Client module (could map to customer entity later)
    private UUID customerId;

    private String orderType; // DINE_IN, TAKEAWAY, DELIVERY
    private String tableNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal taxTotal = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
