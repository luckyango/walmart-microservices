package com.example.orderservice.service;

import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${services.item-service-url}")
    private String itemServiceUrl;

    public CustomerOrder createOrder(Long userId, Long itemId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ItemDto item = restTemplate.getForObject(itemServiceUrl + "/items/" + itemId, ItemDto.class);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        restTemplate.postForObject(itemServiceUrl + "/items/" + itemId + "/decrease?qty=" + qty, null, ItemDto.class);

        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setQuantity(qty);
        order.setTotalPrice(item.getPrice() * qty);
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        System.out.println("[EVENT] OrderCreated: userId=" + userId + ", itemId=" + itemId + ", qty=" + qty);
        return orderRepository.save(order);
    }

    public CustomerOrder updateOrder(Long orderId, int newQty) {
        if (newQty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        CustomerOrder order = getOrder(orderId);
        if (!"CREATED".equals(order.getStatus())) {
            throw new IllegalStateException("Only CREATED order can be updated");
        }

        int delta = newQty - order.getQuantity();
        if (delta > 0) {
            restTemplate.postForObject(itemServiceUrl + "/items/" + order.getItemId() + "/decrease?qty=" + delta, null, ItemDto.class);
        } else if (delta < 0) {
            restTemplate.postForObject(itemServiceUrl + "/items/" + order.getItemId() + "/increase?qty=" + Math.abs(delta), null, ItemDto.class);
        }

        ItemDto item = restTemplate.getForObject(itemServiceUrl + "/items/" + order.getItemId(), ItemDto.class);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        order.setQuantity(newQty);
        order.setTotalPrice(item.getPrice() * newQty);
        System.out.println("[EVENT] OrderUpdated: orderId=" + orderId + ", qty=" + newQty);
        return orderRepository.save(order);
    }

    public CustomerOrder payOrder(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        if (!"CREATED".equals(order.getStatus())) {
            throw new IllegalStateException("Only CREATED order can be paid");
        }
        order.setStatus("PAID");
        System.out.println("[EVENT] PaymentSucceeded: orderId=" + orderId);
        return orderRepository.save(order);
    }

    public CustomerOrder cancelOrder(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        if ("CANCELLED".equals(order.getStatus())) {
            return order;
        }
        if ("PAID".equals(order.getStatus())) {
            System.out.println("[EVENT] RefundRequested: orderId=" + orderId);
        }
        restTemplate.postForObject(itemServiceUrl + "/items/" + order.getItemId() + "/increase?qty=" + order.getQuantity(), null, ItemDto.class);
        order.setStatus("CANCELLED");
        System.out.println("[EVENT] OrderCancelled: orderId=" + orderId);
        return orderRepository.save(order);
    }

    public CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<CustomerOrder> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
