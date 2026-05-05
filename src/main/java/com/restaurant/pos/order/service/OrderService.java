package com.restaurant.pos.order.service;

import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.common.util.SecurityUtils;
import com.restaurant.pos.inventory.service.InventoryService;
import com.restaurant.pos.invoice.domain.Invoice;
import com.restaurant.pos.invoice.domain.InvoiceType;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderLine;
import com.restaurant.pos.order.domain.OrderType;
import com.restaurant.pos.order.domain.Payment;
import com.restaurant.pos.order.domain.PaymentType;
import com.restaurant.pos.invoice.repository.InvoiceRepository;
import com.restaurant.pos.order.repository.OrderRepository;
import com.restaurant.pos.order.repository.PaymentRepository;
import com.restaurant.pos.product.domain.Product;
import com.restaurant.pos.product.repository.ProductRepository;
import com.restaurant.pos.sequence.domain.DocumentType;
import com.restaurant.pos.sequence.service.DocumentSequenceService;
import com.restaurant.pos.table.domain.RestaurantTable;
import com.restaurant.pos.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryService inventoryService;
    private final RestaurantTableRepository tableRepository;
    private final DocumentSequenceService sequenceService;
    private final ProductRepository productRepository;

    private Order hydrateOrderLines(Order order) {
        if (order == null || order.getLines() == null || order.getLines().isEmpty()) {
            return order;
        }

        boolean needsHydration = order.getLines().stream()
                .anyMatch(line -> line.getProductId() != null && (
                        line.getProductName() == null || line.getProductName().isBlank()
                                || line.getCategoryName() == null || line.getCategoryName().isBlank()
                                || line.getIsPackagedGood() == null
                ));

        if (!needsHydration) {
            return order;
        }

        List<UUID> productIds = order.getLines().stream()
                .map(OrderLine::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (productIds.isEmpty()) {
            return order;
        }

        Map<UUID, Product> productsById = productRepository.findByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (OrderLine line : order.getLines()) {
            Product product = productsById.get(line.getProductId());
            if (product == null) {
                continue;
            }

            if (line.getProductName() == null || line.getProductName().isBlank()) {
                line.setProductName(product.getName());
            }
            if (line.getCategoryName() == null || line.getCategoryName().isBlank()) {
                line.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
            }
            if (line.getIsPackagedGood() == null) {
                line.setIsPackagedGood(product.isPackagedGood());
            }
        }

        return order;
    }

    private List<Order> hydrateOrderLines(List<Order> orders) {
        orders.forEach(this::hydrateOrderLines);
        return orders;
    }

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

        return hydrateOrderLines(orders);
    }
    
    public List<Order> getOrders() {
        return getOrders(null);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByType(OrderType orderType) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return hydrateOrderLines(orderRepository.findByClientIdAndOrderTypeOrderByCreatedAtDesc(tenantId, orderType));
        }
        return hydrateOrderLines(orderRepository.findByClientIdAndOrgIdAndOrderTypeOrderByCreatedAtDesc(
                tenantId, TenantContext.getCurrentOrg(), orderType));
    }

    @Transactional(readOnly = true)
    public List<Order> searchOrders(com.restaurant.pos.order.dto.OrderSearchCriteria criteria) {
        UUID clientId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        
        org.springframework.data.jpa.domain.Specification<Order> spec = 
            com.restaurant.pos.order.spec.OrderSpecification.filterBy(criteria, clientId, orgId);
            
        return hydrateOrderLines(orderRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (SecurityUtils.isSuperAdmin()) {
            return hydrateOrderLines(orderRepository.findByIdAndClientId(id, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found or access denied")));
        }
        return hydrateOrderLines(orderRepository.findByIdAndClientIdAndOrgId(id, tenantId, TenantContext.getCurrentOrg())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or access denied")));
    }

    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating order: {} | Tenant: {} | Org: {}", order, TenantContext.getCurrentTenant(), TenantContext.getCurrentOrg());
        
        order.setClientId(TenantContext.getCurrentTenant());
        if (!SecurityUtils.isSuperAdmin() || order.getOrgId() == null) {
            order.setOrgId(TenantContext.getCurrentOrg());
        }

        if (order.getOrderStatus() == null) {
            order.setOrderStatus("DRAFT");
        }

        // Auto-generate orderNo if not provided
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            DocumentType docType = switch (order.getOrderType()) {
                case PURCHASE -> DocumentType.PURCHASE_ORDER;
                case EXPENSE  -> DocumentType.EXPENSE;
                default       -> DocumentType.SALE_ORDER;
            };
            order.setOrderNo(sequenceService.generateNextSequence(docType));
        }

        // Ensure bidirectional mapping
        if (order.getLines() != null) {
            order.getLines().forEach(line -> line.setOrder(order));
        }
        hydrateOrderLines(order);

        Order saved = orderRepository.save(order);
        
        // Always create Invoice for non-draft orders
        if (!"DRAFT".equalsIgnoreCase(saved.getOrderStatus())) {
            generateInvoice(saved);
        }
        
        // Create payment only if completed and paid
        if ("COMPLETED".equalsIgnoreCase(saved.getOrderStatus()) && "PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
            String paymentMethod = saved.getReference() != null ? saved.getReference() : "CASH";
            generatePayment(saved, paymentMethod);
        }

        handleTableStatus(saved);
        return hydrateOrderLines(saved);
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
        hydrateOrderLines(newOrder);
        
        Order saved = orderRepository.save(newOrder);
        
        // 4. Generate new ERP documents (Invoice/Payment)
        UUID oldInvId = oldInvoiceIdList.isEmpty() ? null : oldInvoiceIdList.get(0);
        generateInvoice(saved, oldInvId);
        if ("PAID".equalsIgnoreCase(saved.getPaymentStatus())) {
            String paymentMethod = saved.getReference() != null ? saved.getReference() : "CASH";
            generatePayment(saved, paymentMethod);
        }
        
        handleTableStatus(saved);

        // Inventory Hook: If PURCHASE order is COMPLETED, update stock
        if (saved.getOrderType() == OrderType.PURCHASE && "COMPLETED".equalsIgnoreCase(saved.getOrderStatus())) {
            processInventoryForOrder(saved);
        }
        
        return hydrateOrderLines(saved);
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
            String paymentMethod = result.getReference() != null ? result.getReference() : "CASH";
            generatePayment(result, paymentMethod);
        }
        
        handleTableStatus(result);

        if (result.getOrderType() == OrderType.PURCHASE && "COMPLETED".equalsIgnoreCase(result.getOrderStatus())) {
            processInventoryForOrder(result);
        }
        return hydrateOrderLines(result);
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
        
        // Determine invoice document type from order type
        DocumentType invoiceDocType = switch (order.getOrderType()) {
            case PURCHASE -> DocumentType.VENDOR_BILL;
            case EXPENSE  -> DocumentType.EXPENSE_RECEIPT;
            default       -> DocumentType.CUSTOMER_INVOICE;
        };
        
        String invNo = sequenceService.generateNextSequence(invoiceDocType);
        
        // Map to entity InvoiceType
        InvoiceType invoiceType = switch (invoiceDocType) {
            case VENDOR_BILL -> InvoiceType.VENDOR_BILL;
            case EXPENSE_RECEIPT -> InvoiceType.EXPENSE_RECEIPT;
            default -> InvoiceType.CUSTOMER_INVOICE;
        };

        Invoice invoice = Invoice.builder()
            .invoiceType(invoiceType)
            .terminalId(order.getTerminalId())
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .vendorId(order.getVendorId())
            .invoiceNo(invNo)
            .totalAmount(order.getGrandTotal())
            .amountDue(order.getGrandTotal())
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
        generatePayment(order, "CASH");
    }

    @Transactional
    public void generatePayment(Order order, String paymentMethod) {
        if (order.getPaymentNo() != null && !order.getPaymentNo().isEmpty()) return;

        // Try to find the invoice
        List<Invoice> invoices = invoiceRepository.findByOrderId(order.getId());
        Invoice invoice = invoices.isEmpty() ? generateInvoice(order) : invoices.get(0);

        UUID clientId = order.getClientId();
        UUID orgId = order.getOrgId();
        
        // INBOUND = money received (Sales), OUTBOUND = money paid (Purchase/Expense)
        DocumentType paymentDocType = (order.getOrderType() == OrderType.SALE)
                ? DocumentType.INBOUND_PAYMENT
                : DocumentType.OUTBOUND_PAYMENT;
                
        String payNo = sequenceService.generateNextSequence(paymentDocType);

        PaymentType paymentType = (paymentDocType == DocumentType.INBOUND_PAYMENT)
                ? PaymentType.INBOUND
                : PaymentType.OUTBOUND;
        
        Payment payment = Payment.builder()
            .paymentType(paymentType)
            .terminalId(order.getTerminalId())
            .orderId(order.getId())
            .invoiceId(invoice != null ? invoice.getId() : null)
            .paymentMethod(paymentMethod)
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
