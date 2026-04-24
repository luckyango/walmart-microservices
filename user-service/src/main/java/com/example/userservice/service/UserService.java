package com.example.userservice.service;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AppUser register(AppUser user) {
        validateUser(user);
        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email already exists");
        });
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public LoginResponse login(String email, String password) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isHashedPassword = user.getPassword() != null && user.getPassword().startsWith("$2");
        boolean matches = isHashedPassword
                ? passwordEncoder.matches(password, user.getPassword())
                : user.getPassword().equals(password);

        if (!matches) {
            throw new IllegalArgumentException("Invalid password");
        }

        if (!isHashedPassword) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        return new LoginResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public AppUser getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public AppUser updateUser(Long id, AppUser request) {
        AppUser existing = getUser(id);
        validateUser(request);

        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (!user.getId().equals(id)) {
                throw new IllegalArgumentException("Email already exists");
            }
        });

        existing.setEmail(request.getEmail());
        existing.setUsername(request.getUsername());
        existing.setPassword(passwordEncoder.encode(request.getPassword()));
        existing.setShippingAddress(request.getShippingAddress());
        existing.setBillingAddress(request.getBillingAddress());
        existing.setPaymentMethod(request.getPaymentMethod());
        return userRepository.save(existing);
    }

    private void validateUser(AppUser user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
