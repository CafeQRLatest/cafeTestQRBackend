package com.restaurant.pos.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class RazorpayOrderRequest {
    private BigDecimal amount;
    private String currency = "INR";
    private String receipt;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Map<String, Object> metadata;
}
