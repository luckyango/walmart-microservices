package com.example.orderservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long itemId;
    private int quantity;
    private double totalPrice;
    private String status; // CREATED, PAID, CANCELLED
    private LocalDateTime createdAt;
}
