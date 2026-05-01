package com.example.orderservice.event;

import com.example.orderservice.model.CustomerOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.order-events}")
    private String orderEventsTopic;

    public void publishOrderCreated(CustomerOrder order) {
        publish("ORDER_CREATED", order);
    }

    public void publishOrderCancelled(CustomerOrder order) {
        publish("ORDER_CANCELLED", order);
    }

    private void publish(String eventType, CustomerOrder order) {
        OrderEvent event = new OrderEvent();
        event.setEventType(eventType);
        event.setOrderId(order.getId());
        event.setUserId(order.getUserId());
        event.setItemId(order.getItemId());
        event.setQuantity(order.getQuantity());
        event.setStatus(order.getStatus());
        event.setOccurredAt(LocalDateTime.now());

        try {
            kafkaTemplate.send(orderEventsTopic, String.valueOf(order.getId()), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to publish order event", e);
        }
    }
}
