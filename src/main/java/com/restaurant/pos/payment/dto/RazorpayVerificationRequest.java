package com.restaurant.pos.payment.dto;

import lombok.Data;

@Data
public class RazorpayVerificationRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
