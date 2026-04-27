package com.restaurant.pos.order.service;

import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.invoice.domain.Invoice;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.Payment;
import com.restaurant.pos.invoice.repository.InvoiceRepository;
import com.restaurant.pos.order.repository.OrderRepository;
import com.restaurant.pos.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.pos.common.util.SecurityUtils;
import com.restaurant.pos.inventory.service.InventoryService;
import com.restaurant.pos.table.repository.RestaurantTableRepository;
import com.restaurant.pos.table.domain.RestaurantTable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryService inventoryService;
    private final RestaurantTableRepository tableRepository;

    public List<Order> getOrders(String status) {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<String> statuses = (status != null && !status.isEmpty()) ? Arrays.asList(status.split(",")) : null;

        List<Order> orders;
        if (SecurityUtils.isSuperAdmin()) {
            if (statuses != null) orders = orderRepository.findByClientIdAndOrderStatusInOrderByCreatedAtDesc(tenantId, statuses);
            else orders = orderRepository.findByClientIdOrderByCreatedAtDesc(tenantId);
        } else {
            if (statuses != null) orders = orderRepository.findByClientIdAndOrgIdAndOrderStatusInOrderByCreatedAtDesc(tenantId, TenantContext.getCurrentOrg(), statuses);
            else orders = orderRepository.findByClientIdAndOrgIdOrderByCreatedAtDesc(tenantId, TenantContext.getCurrentOrg());
        }

        // Lazy generate documents for completed orders that are missing them
        orders.stream()
            .filter(o -> "COMPLETED".equalsIgnoreCase(o.getOrderStatus()) && (o.getInvoiceNo() == null || o.getInvoiceNo().isEmpty()))
            .forEach(o -> {
                try { generatePayment(o); } catch (Exception ignored) {}
            });

        return orders;
    }
    
    public List<Order> getOrders() {
        return getOrders(null);
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

        Order saved = orderRepository.save(order);
        
        // Always create Invoice for non-draft orders
        if (!"DRAFT".equalsIgnoreCase(saved.getOrderStatus())) {
            generateInvoice(saved);
        }
        
        // Create payment only if completed and paid
        if ("COMPLETED".equalsIgnoreCase(saved.getOrderStatus()) && "PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
            generatePayment(saved);
        }

        handleTableStatus(saved);
        return saved;
    }

    @Transactional
    public Order updateOrder(UUID id, Order updates) {
        Order oldOrder = getOrder(id);
        
        // 1. Create a deep copy of the old order as a VOID record
        String originalOrderNo = oldOrder.getOrderNo();
        oldOrder.setOrderNo(originalOrderNo + "_VOID_" + (oldOrder.getRevisionNumber() != null ? oldOrder.getRevisionNumber() : 0));
        oldOrder.setOrderStatus("VOID");
        oldOrder.setIsactive("N");
        orderRepository.save(oldOrder);
        
        // 2. VOID the linked invoice
        List<UUID> oldInvoiceIdList = new java.util.ArrayList<>();
        invoiceRepository.findByOrderId(id).forEach(inv -> {
            oldInvoiceIdList.add(inv.getId());
            inv.setInvoiceNo(inv.getInvoiceNo() + "_VOID_" + (oldOrder.getRevisionNumber() != null ? oldOrder.getRevisionNumber() : 0));
            inv.setStatus("VOID");
            invoiceRepository.save(inv);
        });

        // 3. Create NEW order record with the original Order No
        Order newOrder = Order.builder()
            .terminalId(oldOrder.getTerminalId())
            .orderNo(originalOrderNo)
            .orderType(oldOrder.getOrderType())
            .orderStatus(updates.getOrderStatus() != null ? updates.getOrderStatus() : oldOrder.getOrderStatus())
            .paymentStatus(updates.getPaymentStatus() != null ? updates.getPaymentStatus() : oldOrder.getPaymentStatus())
            .orderSource(oldOrder.getOrderSource())
            .fulfillmentType(updates.getFulfillmentType())
            .tableNumber(updates.getTableNumber())
            .tableId(updates.getTableId())
            .customerId(updates.getCustomerId())
            .totalAmount(updates.getTotalAmount())
            .totalTaxAmount(updates.getTotalTaxAmount())
            .totalDiscountAmount(updates.getTotalDiscountAmount())
            .grandTotal(updates.getGrandTotal())
            .description(updates.getDescription())
            .originalOrderId(oldOrder.getId())
            .revisionNumber((oldOrder.getRevisionNumber() != null ? oldOrder.getRevisionNumber() : 0) + 1)
            .build();
            
        // Manually set inherited fields
        newOrder.setClientId(oldOrder.getClientId());
        newOrder.setOrgId(oldOrder.getOrgId());

        if (updates.getLines() != null) {
            updates.getLines().forEach(newOrder::addLine);
        }
        
        Order saved = orderRepository.save(newOrder);
        
        // 4. Generate new ERP documents (Invoice/Payment)
        UUID oldInvId = oldInvoiceIdList.isEmpty() ? null : oldInvoiceIdList.get(0);
        generateInvoice(saved, oldInvId);
        if ("PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
            generatePayment(saved);
        }
        
        handleTableStatus(saved);

        // Inventory Hook: If PURCHASE order is COMPLETED, update stock
        if ("PURCHASE".equalsIgnoreCase(saved.getOrderType()) && "COMPLETED".equalsIgnoreCase(saved.getOrderStatus())) {
            processInventoryForOrder(saved);
        }
        
        return saved;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, String status, String paymentStatus, String description) {
        Order order = getOrder(id);
        if (status != null) order.setOrderStatus(status);
        if (paymentStatus != null) order.setPaymentStatus(paymentStatus);
        if (description != null) order.setDescription(description);
        
        Order result = orderRepository.save(order);
        
        // Generate Invoice if missing for any active status
        if (!"DRAFT".equalsIgnoreCase(result.getOrderStatus()) && !"CANCELLED".equalsIgnoreCase(result.getOrderStatus())) {
            generateInvoice(result);
        }

        // Generate Payment only if completed and paid
        if ("COMPLETED".equalsIgnoreCase(result.getOrderStatus()) && "PAID".equalsIgnoreCase(result.getPaymentStatus())) {
            generatePayment(result);
        }
        
        handleTableStatus(result);

        if ("PURCHASE".equalsIgnoreCase(result.getOrderType()) && "COMPLETED".equalsIgnoreCase(result.getOrderStatus())) {
            processInventoryForOrder(result);
        }
        return result;
    }

    @Transactional
    public Order updateOrderStatus(UUID id, String status) {
        return updateOrderStatus(id, status, null, null);
    }

    @Transactional
    public Invoice generateInvoice(Order order) {
        return generateInvoice(order, null);
    }

    @Transactional
    public Invoice generateInvoice(Order order, UUID originalInvoiceId) {
        // Return existing if found (checked via Formula or direct repo check)
        // Since we are using @Formula, we can check if it already has one
        if (order.getInvoiceNo() != null && !order.getInvoiceNo().isEmpty()) return null;

        UUID clientId = order.getClientId();
        UUID orgId = order.getOrgId();
        
        long invCount = invoiceRepository.countByClientId(clientId) + 1;
        String invNo = String.format("INV-%d-%05d", LocalDateTime.now().getYear(), invCount);
        
        Invoice invoice = Invoice.builder()
            .terminalId(order.getTerminalId())
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .vendorId(order.getVendorId())
            .invoiceNo(invNo)
            .totalAmount(order.getGrandTotal())
            .amountDue(order.getGrandTotal()) // Initially due
            .status("UNPAID")
            .isPaid(false)
            .isCredit("PENDING".equalsIgnoreCase(order.getPaymentStatus()))
            .originalInvoiceId(originalInvoiceId)
            .build();
            
        invoice.setClientId(clientId);
        invoice.setOrgId(orgId);
            
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void generatePayment(Order order) {
        if (order.getPaymentNo() != null && !order.getPaymentNo().isEmpty()) return;

        // Try to find the invoice
        List<Invoice> invoices = invoiceRepository.findByOrderId(order.getId());
        Invoice invoice = invoices.isEmpty() ? generateInvoice(order) : invoices.get(0);

        UUID clientId = order.getClientId();
        UUID orgId = order.getOrgId();
        
        long payCount = paymentRepository.countByClientId(clientId) + 1;
        String payNo = String.format("PAY-%d-%05d", LocalDateTime.now().getYear(), payCount);
        
        Payment payment = Payment.builder()
            .terminalId(order.getTerminalId())
            .orderId(order.getId())
            .invoiceId(invoice != null ? invoice.getId() : null)
            .paymentMethod("CASH") // Default
            .amountPaid(order.getGrandTotal())
            .referenceNo(payNo)
            .status("COMPLETED")
            .build();
            
        payment.setClientId(clientId);
        payment.setOrgId(orgId);
            
        paymentRepository.save(payment);
        
        // Update Invoice status if it exists
        if (invoice != null) {
            invoice.setStatus("PAID");
            invoice.setIsPaid(true);
            invoice.setAmountDue(BigDecimal.ZERO);
            invoiceRepository.save(invoice);
        }
    }

    private void handleTableStatus(Order order) {
        if (order.getTableId() == null) return;

        // If order is COMPLETED, CANCELLED or PAID, release table
        boolean shouldRelease = "COMPLETED".equalsIgnoreCase(order.getOrderStatus()) || 
                                "CANCELLED".equalsIgnoreCase(order.getOrderStatus()) ||
                                "PAID".equalsIgnoreCase(order.getPaymentStatus());

        tableRepository.findById(order.getTableId()).ifPresent(table -> {
            String newStatus = shouldRelease ? "AVAILABLE" : "OCCUPIED";
            if (!newStatus.equals(table.getStatus())) {
                table.setStatus(newStatus);
                tableRepository.save(table);
            }
        });
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
