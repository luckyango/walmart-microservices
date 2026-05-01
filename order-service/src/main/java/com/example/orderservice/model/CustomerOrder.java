package com.example.orderservice.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerOrder {
    private Long id;
    private Long userId;
    private String itemId;
    private int quantity;
    private double totalPrice;
    private String status; // CREATED, PAID, CANCELLED
    private LocalDateTime createdAt;
}
