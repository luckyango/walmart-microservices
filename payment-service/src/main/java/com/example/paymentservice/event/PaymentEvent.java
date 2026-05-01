package com.example.paymentservice.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentEvent {
    private String eventType;
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private Double amount;
    private String status;
    private LocalDateTime occurredAt;
}
