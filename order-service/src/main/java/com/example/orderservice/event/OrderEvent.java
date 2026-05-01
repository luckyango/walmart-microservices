package com.example.orderservice.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderEvent {
    private String eventType;
    private Long orderId;
    private Long userId;
    private String itemId;
    private Integer quantity;
    private String status;
    private LocalDateTime occurredAt;
}
