package com.example.paymentservice.dto;

import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private Long userId;
    private double amount;
    private String paymentMethod;
    private String idempotencyKey;
}
