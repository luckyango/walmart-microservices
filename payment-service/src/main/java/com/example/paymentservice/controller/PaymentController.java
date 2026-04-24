package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public Payment submitPayment(@RequestBody CreatePaymentRequest request) {
        ensureUserAccess(request.getUserId());
        return paymentService.submitPayment(request);
    }

    @PostMapping("/{id}/refund")
    public Payment refundPayment(@PathVariable("id") Long id) {
        Payment payment = paymentService.getPayment(id);
        ensureUserAccess(payment.getUserId());
        return paymentService.refundPayment(id);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable("id") Long id) {
        Payment payment = paymentService.getPayment(id);
        ensureUserAccess(payment.getUserId());
        return payment;
    }

    private void ensureUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.getName().equals(String.valueOf(userId))) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }
    }
}
