package com.example.orderservice.service;

import com.example.orderservice.client.ItemServiceClient;
import com.example.orderservice.client.PaymentServiceClient;
import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.event.OrderEventPublisher;
import com.example.orderservice.exception.ServiceUnavailableException;
import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.repository.OrderRepository;
import feign.FeignException;
import feign.RetryableException;
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
    private final OrderEventPublisher orderEventPublisher;

    public CustomerOrder createOrder(Long userId, String itemId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        ItemDto item = getItemOrThrow(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        decreaseInventoryOrThrow(itemId, qty);

        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setItemId(itemId);
        order.setQuantity(qty);
        order.setTotalPrice(item.getPrice() * qty);
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        System.out.println("[EVENT] OrderCreated: userId=" + userId + ", itemId=" + itemId + ", qty=" + qty);
        CustomerOrder saved = orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(saved);
        return saved;
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
            decreaseInventoryOrThrow(order.getItemId(), delta);
        } else if (delta < 0) {
            increaseInventoryOrThrow(order.getItemId(), Math.abs(delta));
        }

        ItemDto item = getItemOrThrow(order.getItemId());
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
        if ("PAID".equals(order.getStatus())) {
            return order;
        }
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
            refundPaymentOrThrow(orderId, authorizationHeader);
            System.out.println("[EVENT] RefundRequested: orderId=" + orderId);
        }

        increaseInventoryOrThrow(order.getItemId(), order.getQuantity());
        order.setStatus("CANCELLED");
        System.out.println("[EVENT] OrderCancelled: orderId=" + orderId);
        CustomerOrder saved = orderRepository.save(order);
        orderEventPublisher.publishOrderCancelled(saved);
        return saved;
    }

    public CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    public List<CustomerOrder> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public CustomerOrder markPaidFromPaymentEvent(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        if ("PAID".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            return order;
        }
        order.setStatus("PAID");
        System.out.println("[KAFKA] Payment event marked order as PAID: orderId=" + orderId);
        return orderRepository.save(order);
    }

    public CustomerOrder syncRefundedFromPaymentEvent(Long orderId) {
        CustomerOrder order = getOrder(orderId);
        if ("CANCELLED".equals(order.getStatus())) {
            return order;
        }
        if ("PAID".equals(order.getStatus())) {
            order.setStatus("CANCELLED");
            System.out.println("[KAFKA] Refund event marked order as CANCELLED: orderId=" + orderId);
            return orderRepository.save(order);
        }
        return order;
    }

    private ItemDto getItemOrThrow(String itemId) {
        try {
            return itemServiceClient.getItem(itemId);
        } catch (FeignException.NotFound ex) {
            throw new IllegalArgumentException("Item not found");
        } catch (RetryableException ex) {
            throw new ServiceUnavailableException("Item service is temporarily unavailable");
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("Unable to reach item service right now");
        }
    }

    private void decreaseInventoryOrThrow(String itemId, int qty) {
        try {
            itemServiceClient.decreaseInventory(itemId, qty);
        } catch (FeignException.NotFound ex) {
            throw new IllegalArgumentException("Item not found");
        } catch (FeignException.Conflict ex) {
            throw new IllegalStateException("Item is sold out");
        } catch (RetryableException ex) {
            throw new ServiceUnavailableException("Inventory service is temporarily unavailable");
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("Unable to update inventory right now");
        }
    }

    private void increaseInventoryOrThrow(String itemId, int qty) {
        try {
            itemServiceClient.increaseInventory(itemId, qty);
        } catch (RetryableException ex) {
            throw new ServiceUnavailableException("Inventory service is temporarily unavailable");
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("Unable to restore inventory right now");
        }
    }

    private void refundPaymentOrThrow(Long orderId, String authorizationHeader) {
        try {
            paymentServiceClient.refundPaymentByOrderId(orderId, authorizationHeader);
        } catch (RetryableException ex) {
            throw new ServiceUnavailableException("Payment service is temporarily unavailable");
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("Unable to contact payment service right now");
        }
    }
}
