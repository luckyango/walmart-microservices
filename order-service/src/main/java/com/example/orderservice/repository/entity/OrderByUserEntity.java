package com.example.orderservice.repository.entity;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Data
@Table("orders_by_user")
public class OrderByUserEntity {
    @PrimaryKey
    private OrderByUserKey key;

    private String itemId;
    private Integer quantity;
    private Double totalPrice;
    private String status;
}
