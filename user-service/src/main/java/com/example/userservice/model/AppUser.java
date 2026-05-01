package com.example.userservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username;
    private String password;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    @Column(length = 512)
    private String refreshToken;
    private LocalDateTime refreshTokenExpiresAt;
}
