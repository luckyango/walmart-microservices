package com.example.orderservice.service;

import com.example.orderservice.client.ItemServiceClient;
import com.example.orderservice.client.PaymentServiceClient;
import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemServiceClient itemServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    public CustomerOrder createOrder(Long userId, String itemId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ItemDto item = itemServiceClient.getItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        itemServiceClient.decreaseInventory(itemId, qty);

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
            itemServiceClient.decreaseInventory(order.getItemId(), delta);
        } else if (delta < 0) {
            itemServiceClient.increaseInventory(order.getItemId(), Math.abs(delta));
        }

        ItemDto item = itemServiceClient.getItem(order.getItemId());
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

    public CustomerOrder cancelOrder(Long orderId, String authorizationHeader) {
        CustomerOrder order = getOrder(orderId);
        if ("CANCELLED".equals(order.getStatus())) {
            return order;
        }
        if (!"CREATED".equals(order.getStatus()) && !"PAID".equals(order.getStatus())) {
            throw new IllegalStateException("Only CREATED or PAID order can be cancelled");
        }

        if ("PAID".equals(order.getStatus())) {
            paymentServiceClient.refundPaymentByOrderId(orderId, authorizationHeader);
            System.out.println("[EVENT] RefundRequested: orderId=" + orderId);
        }

        itemServiceClient.increaseInventory(order.getItemId(), order.getQuantity());
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
