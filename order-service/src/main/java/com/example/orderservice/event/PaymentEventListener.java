package com.example.orderservice.event;

import com.example.orderservice.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.payment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void onPaymentEvent(String payload) {
        try {
            PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
            if ("PAYMENT_PAID".equals(event.getEventType())) {
                orderService.markPaidFromPaymentEvent(event.getOrderId());
            } else if ("PAYMENT_REFUNDED".equals(event.getEventType())) {
                orderService.syncRefundedFromPaymentEvent(event.getOrderId());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse payment event payload: {}", payload, e);
        }
    }
}
