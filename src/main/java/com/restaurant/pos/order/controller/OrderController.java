package com.restaurant.pos.order.controller;

import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderType;
import com.restaurant.pos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
 
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrders(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(status)));
    }  

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByType(@PathVariable String type) {
        try {
            OrderType orderType = OrderType.valueOf(type.toUpperCase());
            return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByType(orderType)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid order type: " + type + ". Valid values: SALE, PURCHASE, EXPENSE"));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(order)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Order>> updateOrder(@PathVariable UUID id, @RequestBody Order order) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrder(id, order)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String description
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(id, status, paymentStatus, description)));
    }
}
