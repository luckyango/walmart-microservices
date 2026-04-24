package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public Payment submitPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.submitPayment(request);
    }

    @PostMapping("/{id}/refund")
    public Payment refundPayment(@PathVariable("id") Long id) {
        return paymentService.refundPayment(id);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable("id") Long id) {
        return paymentService.getPayment(id);
    }
}
