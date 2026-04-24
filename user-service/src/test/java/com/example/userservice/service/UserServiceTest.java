package com.example.userservice.service;

import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerShouldPersistValidUser() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = userService.register(user);

        assertEquals("nova@test.com", saved.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void loginShouldRejectWrongPassword() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.login(user.getEmail(), "wrong"));

        assertEquals("Invalid password", ex.getMessage());
    }

    @Test
    void updateShouldRejectEmailOwnedByAnotherUser() {
        AppUser existing = buildUser();
        existing.setId(1L);
        AppUser other = buildUser();
        other.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail(existing.getEmail())).thenReturn(Optional.of(other));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(1L, existing));

        assertEquals("Email already exists", ex.getMessage());
    }

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("nova@test.com");
        user.setUsername("Nova");
        user.setPassword("123456");
        user.setShippingAddress("Durham, NC");
        user.setBillingAddress("Durham, NC");
        user.setPaymentMethod("Visa");
        return user;
    }
}
