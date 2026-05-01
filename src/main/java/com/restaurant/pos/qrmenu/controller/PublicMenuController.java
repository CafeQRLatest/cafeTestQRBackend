package com.restaurant.pos.qrmenu.controller;

import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderLine;
import com.restaurant.pos.order.domain.OrderType;
import com.restaurant.pos.order.repository.OrderRepository;
import com.restaurant.pos.product.domain.Product;
import com.restaurant.pos.product.repository.ProductRepository;
import com.restaurant.pos.table.domain.RestaurantTable;
import com.restaurant.pos.table.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Public (unauthenticated) API for the QR-code customer ordering flow.
 * Customers scan a QR code containing clientId/orgId/tableId, which opens
 * the digital menu. They can browse products and place orders — all without
 * needing to log in.
 */
@RestController
@RequestMapping("/api/v1/public/menu")
@RequiredArgsConstructor
public class PublicMenuController {

    private final ProductRepository productRepository;
    private final RestaurantTableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final com.restaurant.pos.client.repository.ClientRepository clientRepository;
    private final com.restaurant.pos.client.repository.OrganizationRepository organizationRepository;

    /**
     * GET /api/v1/public/menu/{clientId}/{orgId}
     * Returns the full active menu for a given restaurant (client + org).
     */
    @GetMapping("/{clientId}/{orgId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMenu(
            @PathVariable UUID clientId,
            @PathVariable String orgId) {

        UUID orgUuid = (orgId == null || "null".equals(orgId)) ? null : UUID.fromString(orgId);
        List<Product> products = productRepository
                .findByClientIdAndOrgIdOrGlobalAndIsActiveTrue(clientId, orgUuid);

        // Filter to only available products and map to a lightweight DTO
        List<Map<String, Object>> menu = products.stream()
                .filter(Product::isAvailable)
                .map(p -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("description", p.getDescription());
                    item.put("price", p.getPrice());
                    item.put("imageUrl", p.getImageUrl());
                    item.put("category", p.getCategory() != null ? p.getCategory().getName() : "Others");
                    item.put("isVeg", !p.isPackagedGood()); // Use productType or a dedicated field
                    item.put("isAvailable", p.isAvailable());
                    item.put("productType", p.getProductType());
                    return item;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(menu));
    }

    /**
     * GET /api/v1/public/menu/{clientId}/{orgId}/table/{tableId}
     * Returns table info for the scanned QR code.
     */
    @GetMapping("/{clientId}/{orgId}/table/{tableId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTableInfo(
            @PathVariable UUID clientId,
            @PathVariable String orgId,
            @PathVariable UUID tableId) {

        UUID orgUuid = (orgId == null || "null".equals(orgId)) ? null : UUID.fromString(orgId);
        Optional<RestaurantTable> tableOpt = tableRepository.findById(tableId);

        if (tableOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "found", false,
                    "message", "Table not found"
            )));
        }

        RestaurantTable table = tableOpt.get();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("found", true);
        info.put("id", table.getId());
        info.put("tableNumber", table.getTableNumber());
        info.put("name", table.getName());
        info.put("status", table.getStatus());
        info.put("seatingCapacity", table.getSeatingCapacity());
        info.put("floor", table.getFloor());
        info.put("section", table.getSection());

        // Attach brand color, name, and logo (Prefer Organization-specific, fallback to Client-global)
        UUID effectiveClientId = table.getClientId() != null ? table.getClientId() : clientId;
        UUID effectiveOrgId = table.getOrgId() != null ? table.getOrgId() : orgUuid;

        clientRepository.findById(effectiveClientId).ifPresent(client -> {
            info.put("brandColor", client.getBrandColor() != null ? client.getBrandColor() : "#f97316");
            info.put("restaurantName", client.getName() != null ? client.getName() : "Our Restaurant");
            info.put("logoUrl", client.getLogoUrl());
        });

        // Override with Organization info if available
        if (effectiveOrgId != null) {
            organizationRepository.findById(effectiveOrgId).ifPresent(org -> {
                if (org.getLogoUrl() != null && !org.getLogoUrl().isBlank()) {
                    info.put("logoUrl", org.getLogoUrl());
                }
                if (org.getName() != null && !org.getName().isBlank()) {
                    info.put("restaurantName", org.getName());
                }
            });
        }

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * POST /api/v1/public/menu/{clientId}/{orgId}/order
     * Places an order from the QR menu (no auth required).
     */
    @PostMapping("/{clientId}/{orgId}/order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeOrder(
            @PathVariable UUID clientId,
            @PathVariable String orgId,
            @RequestBody Map<String, Object> payload) {

        UUID orgUuid = (orgId == null || "null".equals(orgId)) ? null : UUID.fromString(orgId);

        String tableNumber = (String) payload.getOrDefault("tableNumber", "QR");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        String customerNote = (String) payload.getOrDefault("note", "");

        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.success(Map.of("error", "No items in order")));
        }

        // Generate order number
        String orderNo = "QR-" + System.currentTimeMillis();

        // Build the order
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .orderNo(orderNo)
                .orderType(OrderType.SALE)   // QR menu orders are always customer sales
                .orderStatus("CONFIRMED")
                .paymentStatus("PENDING")
                .orderSource("QR_MENU")
                .fulfillmentType("DINE_IN")
                .tableNumber(tableNumber)
                .description(customerNote)
                .orderDate(LocalDateTime.now())
                .build();
                
        String tableIdStr = (String) payload.get("tableId");
        if (tableIdStr != null && !tableIdStr.isBlank()) {
            order.setTableId(UUID.fromString(tableIdStr));
        }
                
        String customerIdStr = (String) payload.get("customerId");
        if (customerIdStr != null && !customerIdStr.isBlank()) {
            order.setCustomerId(UUID.fromString(customerIdStr));
        }
        
        order.setClientId(clientId);
        order.setOrgId(orgUuid);

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Map<String, Object> cartItem : items) {
            UUID productId = UUID.fromString((String) cartItem.get("productId"));
            int qty = ((Number) cartItem.get("quantity")).intValue();
            BigDecimal price = new BigDecimal(cartItem.get("price").toString());
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            OrderLine line = OrderLine.builder()
                    .productId(productId)
                    .quantity(BigDecimal.valueOf(qty))
                    .unitPrice(price)
                    .lineTotal(lineTotal)
                    .build();
            order.addLine(line);

            grandTotal = grandTotal.add(lineTotal);
        }

        order.setTotalAmount(grandTotal);
        order.setGrandTotal(grandTotal);

        Order saved = orderRepository.save(order);

        // Update table status to OCCUPIED
        if (tableIdStr != null && !tableIdStr.isBlank()) {
            try {
                UUID tid = UUID.fromString(tableIdStr);
                tableRepository.findByIdAndClientIdAndOrgId(tid, clientId, orgUuid).ifPresent(tbl -> {
                    if (!"OCCUPIED".equals(tbl.getStatus())) {
                        tbl.setStatus("OCCUPIED");
                        tableRepository.save(tbl);
                    }
                });
            } catch (Exception e) {
                // Ignore invalid UUID or other errors
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", saved.getId());
        response.put("orderNo", saved.getOrderNo());
        response.put("status", saved.getOrderStatus());
        response.put("grandTotal", saved.getGrandTotal());
        response.put("tableNumber", saved.getTableNumber());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
