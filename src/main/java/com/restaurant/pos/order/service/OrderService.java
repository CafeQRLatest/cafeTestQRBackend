package com.restaurant.pos.order.service;

import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.pos.common.util.SecurityUtils;
import com.restaurant.pos.inventory.service.InventoryService;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public List<Order> getOrders() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return orderRepository.findByClientIdOrderByCreatedAtDesc(tenantId);
        }
        return orderRepository.findByClientIdAndOrgIdOrderByCreatedAtDesc(tenantId, TenantContext.getCurrentOrg());
    }

    public List<Order> getOrdersByType(String orderType) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return orderRepository.findByClientIdAndOrderTypeOrderByCreatedAtDesc(tenantId, orderType);
        }
        return orderRepository.findByClientIdAndOrgIdAndOrderTypeOrderByCreatedAtDesc(
                tenantId, TenantContext.getCurrentOrg(), orderType);
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

        if (order.getOrderStatus() == null) {
            order.setOrderStatus("DRAFT");
        }

        // Ensure bidirectional mapping
        if (order.getLines() != null) {
            order.getLines().forEach(line -> line.setOrder(order));
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(UUID id, Order updates) {
        Order existing = getOrder(id);
        existing.setOrderStatus(updates.getOrderStatus());
        existing.setPaymentStatus(updates.getPaymentStatus());
        existing.setCustomerId(updates.getCustomerId());
        existing.setVendorId(updates.getVendorId());
        existing.setDescription(updates.getDescription());
        existing.setReference(updates.getReference());
        existing.setTotalTaxAmount(updates.getTotalTaxAmount());
        existing.setTotalDiscountAmount(updates.getTotalDiscountAmount());
        existing.setTotalAmount(updates.getTotalAmount());
        existing.setGrandTotal(updates.getGrandTotal());

        // Update lines
        if (updates.getLines() != null) {
            existing.getLines().clear();
            updates.getLines().forEach(line -> {
                line.setOrder(existing);
                existing.getLines().add(line);
            });
        }

        Order result = orderRepository.save(existing);
        
        // Inventory Hook: If PURCHASE order is COMPLETED, update stock
        if ("PURCHASE".equalsIgnoreCase(result.getOrderType()) && "COMPLETED".equalsIgnoreCase(result.getOrderStatus())) {
            processInventoryForOrder(result);
        }
        
        return result;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, String status) {
        Order order = getOrder(id);
        order.setOrderStatus(status);
        Order result = orderRepository.save(order);
        
        // Inventory Hook
        if ("PURCHASE".equalsIgnoreCase(result.getOrderType()) && "COMPLETED".equalsIgnoreCase(result.getOrderStatus())) {
            processInventoryForOrder(result);
        }
        
        return result;
    }

    private void processInventoryForOrder(Order order) {
        if (order.getWarehouseId() == null) {
            // Log warning or throw exception? For now, we need a warehouse to receive stock.
            return;
        }

        if (order.getLines() != null) {
            for (com.restaurant.pos.order.domain.OrderLine line : order.getLines()) {
                inventoryService.updateStock(
                    order.getWarehouseId(),
                    line.getProductId(),
                    line.getVariantId(),
                    line.getQuantity(),
                    "PURCHASE",
                    order.getId(),
                    line.getUnitPrice()
                );
            }
        }
    }
}
