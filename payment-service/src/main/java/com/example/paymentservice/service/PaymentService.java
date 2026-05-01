package com.example.paymentservice.service;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.event.PaymentEventPublisher;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public Payment submitPayment(CreatePaymentRequest request) {
        validateCreateRequest(request);

        return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .orElseGet(() -> {
                    Payment payment = new Payment();
                    payment.setOrderId(request.getOrderId());
                    payment.setUserId(request.getUserId());
                    payment.setAmount(request.getAmount());
                    payment.setPaymentMethod(request.getPaymentMethod());
                    payment.setIdempotencyKey(request.getIdempotencyKey());
                    payment.setStatus("PAID");
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setUpdatedAt(LocalDateTime.now());
                    System.out.println("[EVENT] PaymentPaid: orderId=" + request.getOrderId());
                    Payment saved = paymentRepository.save(payment);
                    paymentEventPublisher.publishPaymentPaid(saved);
                    return saved;
                });
    }

    public Payment refundPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if ("REFUNDED".equals(payment.getStatus())) {
            return payment;
        }
        if (!"PAID".equals(payment.getStatus())) {
            throw new IllegalStateException("Only PAID payment can be refunded");
        }
        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());
        System.out.println("[EVENT] PaymentRefunded: paymentId=" + paymentId);
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentRefunded(saved);
        return saved;
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
    }

    public Payment refundPaymentByOrderId(Long orderId) {
        Payment payment = getPaymentByOrderId(orderId);
        return refundPayment(payment.getId());
    }

    private void validateCreateRequest(CreatePaymentRequest request) {
        if (request.getOrderId() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("orderId and userId are required");
        }
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
    }
}
