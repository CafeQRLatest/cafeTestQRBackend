package com.restaurant.pos.subscription.dto;

import lombok.Data;

@Data
public class SubscriptionActivationRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
