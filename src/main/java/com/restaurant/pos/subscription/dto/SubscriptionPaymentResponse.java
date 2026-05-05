package com.restaurant.pos.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPaymentResponse {
    private String orderId;
    private long amount;
    private String currency;
    private String keyId;
    private String planName;
    private String description;
}
