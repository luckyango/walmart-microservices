package com.example.orderservice.repository.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Data
@Table("orders_by_id")
public class OrderByIdEntity {
    @PrimaryKey("order_id")
    private Long orderId;

    private Long userId;
    private String itemId;
    private Integer quantity;
    private Double totalPrice;
    private String status;
    private Instant createdAt;
}
