package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthSession {
    private LoginResponse response;
    private String refreshToken;
}
