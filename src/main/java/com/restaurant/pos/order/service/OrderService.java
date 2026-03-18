package com.restaurant.pos.order.service;

import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderStatus;
import com.restaurant.pos.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.pos.common.util.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public List<Order> getOrders() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return orderRepository.findByClientIdOrderByCreatedAtDesc(tenantId);
        }
        return orderRepository.findByClientIdAndOrgIdOrderByCreatedAtDesc(tenantId, TenantContext.getCurrentOrg());
    }

    public Order getOrder(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return orderRepository.findByIdAndClientId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found or access denied"));
        }
        return orderRepository.findByIdAndClientIdAndOrgId(id, tenantId, TenantContext.getCurrentOrg())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or access denied"));
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setClientId(TenantContext.getCurrentTenant());
        if (!SecurityUtils.isSuperAdmin() || order.getOrgId() == null) {
            order.setOrgId(TenantContext.getCurrentOrg());
        }

        // If an idempotency key is provided, check if the order already exists to support offline sync safely
        if (order.getIdempotencyKey() != null) {
            Optional<Order> existingOrder;
            if (SecurityUtils.isSuperAdmin()) {
                existingOrder = orderRepository.findByIdempotencyKeyAndClientId(
                        order.getIdempotencyKey(), order.getClientId()
                );
            } else {
                existingOrder = orderRepository.findByIdempotencyKeyAndClientIdAndOrgId(
                        order.getIdempotencyKey(), order.getClientId(), order.getOrgId()
                );
            }
            
            if (existingOrder.isPresent()) {
                return existingOrder.get();
            }
        }

        order.setStatus(OrderStatus.NEW);
        
        // Ensure bidirectional mapping is set up correctly
        if (order.getItems() != null) {
            order.getItems().forEach(item -> item.setOrder(order));
        }

        Order savedOrder = orderRepository.save(order);

        // Publish event to RabbitMQ
        rabbitTemplate.convertAndSend("pos.exchange", "order.created", 
            "Order created: " + savedOrder.getId() + " for Client: " + savedOrder.getClientId()
        );

        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, OrderStatus status) {
        Order order = getOrder(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
