package com.example.orderservice.dto;

import lombok.Data;

@Data
public class ItemDto {
    private String id;
    private String name;
    private String upc;
    private String pictureUrl;
    private double price;
    private int inventory;
}
