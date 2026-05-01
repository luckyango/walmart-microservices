package com.example.aiservice.dto;

import lombok.Data;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private String itemId;
    private int quantity;
    private double totalPrice;
    private String status;
    private String createdAt;
}
