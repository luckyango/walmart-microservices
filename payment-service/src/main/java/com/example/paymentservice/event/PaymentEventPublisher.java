package com.example.paymentservice.event;

import com.example.paymentservice.model.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    public void publishPaymentPaid(Payment payment) {
        publish("PAYMENT_PAID", payment);
    }

    public void publishPaymentRefunded(Payment payment) {
        publish("PAYMENT_REFUNDED", payment);
    }

    private void publish(String eventType, Payment payment) {
        PaymentEvent event = new PaymentEvent();
        event.setEventType(eventType);
        event.setPaymentId(payment.getId());
        event.setOrderId(payment.getOrderId());
        event.setUserId(payment.getUserId());
        event.setAmount(payment.getAmount());
        event.setStatus(payment.getStatus());
        event.setOccurredAt(LocalDateTime.now());
        try {
            kafkaTemplate.send(paymentEventsTopic, String.valueOf(payment.getOrderId()), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to publish payment event", e);
        }
    }
}
