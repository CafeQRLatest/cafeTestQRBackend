package com.restaurant.pos.payment.controller;

import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.common.exception.BusinessException;
import com.restaurant.pos.payment.dto.RazorpayOrderRequest;
import com.restaurant.pos.payment.dto.RazorpayOrderResponse;
import com.restaurant.pos.payment.dto.RazorpayVerificationRequest;
import com.restaurant.pos.payment.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/payments")
@RequiredArgsConstructor
public class PublicPaymentController {

    private final RazorpayService razorpayService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(@RequestBody RazorpayOrderRequest request) {
        Map<String, Object> notes = new LinkedHashMap<>();
        if (request.getMetadata() != null) {
            notes.putAll(request.getMetadata());
        }
        if (request.getCustomerName() != null) notes.put("customer_name", request.getCustomerName());
        if (request.getCustomerEmail() != null) notes.put("customer_email", request.getCustomerEmail());
        if (request.getCustomerPhone() != null) notes.put("customer_phone", request.getCustomerPhone());

        RazorpayOrderResponse order = razorpayService.createOrder(
                request.getAmount(),
                request.getCurrency(),
                request.getReceipt(),
                notes
        );

        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verify(@RequestBody RazorpayVerificationRequest request) {
        boolean valid = razorpayService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!valid) {
            throw new BusinessException("Razorpay payment signature verification failed");
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "verified", true,
                "razorpayOrderId", request.getRazorpayOrderId(),
                "razorpayPaymentId", request.getRazorpayPaymentId()
        )));
    }
}
