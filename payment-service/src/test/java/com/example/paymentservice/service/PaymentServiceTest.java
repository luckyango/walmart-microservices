package com.example.paymentservice.service;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.event.PaymentEventPublisher;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void submitShouldReturnExistingPaymentForSameIdempotencyKey() {
        CreatePaymentRequest request = buildRequest();
        Payment payment = buildPayment();
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(payment));

        Payment result = paymentService.submitPayment(request);

        assertEquals(99L, result.getId());
        verify(paymentRepository).findByIdempotencyKey("idem-1");
    }

    @Test
    void submitShouldRejectBlankIdempotencyKey() {
        CreatePaymentRequest request = buildRequest();
        request.setIdempotencyKey(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.submitPayment(request));

        assertEquals("idempotencyKey is required", ex.getMessage());
    }

    @Test
    void refundShouldUpdateStatus() {
        Payment payment = buildPayment();
        when(paymentRepository.findById(99L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment refunded = paymentService.refundPayment(99L);

        assertEquals("REFUNDED", refunded.getStatus());
    }

    private CreatePaymentRequest buildRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(1L);
        request.setUserId(2L);
        request.setAmount(49.99);
        request.setPaymentMethod("Visa");
        request.setIdempotencyKey("idem-1");
        return request;
    }

    private Payment buildPayment() {
        Payment payment = new Payment();
        payment.setId(99L);
        payment.setOrderId(1L);
        payment.setUserId(2L);
        payment.setAmount(49.99);
        payment.setPaymentMethod("Visa");
        payment.setIdempotencyKey("idem-1");
        payment.setStatus("PAID");
        return payment;
    }
}
