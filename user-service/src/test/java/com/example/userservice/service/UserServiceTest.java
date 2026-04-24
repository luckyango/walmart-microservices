package com.example.userservice.service;

import com.example.userservice.model.AppUser;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerShouldPersistValidUser() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = userService.register(user);

        assertEquals("nova@test.com", saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
    }

    @Test
    void loginShouldRejectWrongPassword() {
        AppUser user = buildUser();
        user.setPassword("encoded-password");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

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

    @Test
    void loginShouldUpgradeLegacyPlainTextPassword() {
        AppUser user = buildUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        userService.login(user.getEmail(), "123456");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encoded-password", captor.getValue().getPassword());
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
