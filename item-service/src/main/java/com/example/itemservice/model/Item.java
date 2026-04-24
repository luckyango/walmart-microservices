package com.example.itemservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String upc;
    private String pictureUrl;
    private double price;
    private int inventory;
}
