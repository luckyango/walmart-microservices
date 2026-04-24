package com.example.itemservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "items")
@Data
public class Item {
    @Id
    private String id;

    private String name;
    private String upc;
    private String pictureUrl;
    private double price;
    private int inventory;
}
