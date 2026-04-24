package com.example.userservice.controller;

import com.example.userservice.dto.LoginResponse;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.AppUser;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@RequestBody AppUser user) {
        return UserResponse.from(userService.register(user));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestParam("email") String email, @RequestParam("password") String password) {
        return userService.login(email, password);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody AppUser user) {
        return UserResponse.from(userService.updateUser(id, user));
    }
}
