package com.example.userservice.dto;

import com.example.userservice.model.AppUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getShippingAddress(),
                user.getBillingAddress(),
                user.getPaymentMethod()
        );
    }
}
