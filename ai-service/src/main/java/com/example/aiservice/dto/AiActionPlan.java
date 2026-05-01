package com.example.aiservice.dto;

import lombok.Data;

@Data
public class AiActionPlan {
    private String action;
    private String itemQuery;
    private Integer quantity;
    private String reply;
}
