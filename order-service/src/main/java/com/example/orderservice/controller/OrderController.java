package com.example.orderservice.controller;

import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.dto.UpdateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public CustomerOrder createOrder(@RequestParam("userId") Long userId,
                                     @RequestParam("itemId") String itemId,
                                     @RequestParam("qty") int qty) {
        ensureUserAccess(userId);
        return orderService.createOrder(userId, itemId, qty);
    }

    @PostMapping("/{id}/pay")
    public CustomerOrder payOrder(@PathVariable("id") Long id) {
        ensureOrderOwner(id);
        return orderService.payOrder(id);
    }

    @PutMapping("/{id}")
    public CustomerOrder updateOrder(@PathVariable("id") Long id, @RequestBody UpdateOrderRequest request) {
        ensureOrderOwner(id);
        return orderService.updateOrder(id, request.getQuantity());
    }

    @PostMapping("/{id}/cancel")
    public CustomerOrder cancelOrder(@PathVariable("id") Long id) {
        ensureOrderOwner(id);
        return orderService.cancelOrder(id);
    }

    @GetMapping("/{id}")
    public CustomerOrder getOrder(@PathVariable("id") Long id) {
        ensureOrderOwner(id);
        return orderService.getOrder(id);
    }

    @GetMapping("/user/{userId}")
    public List<CustomerOrder> getOrdersByUser(@PathVariable("userId") Long userId) {
        ensureUserAccess(userId);
        return orderService.getOrdersByUser(userId);
    }

    private void ensureOrderOwner(Long orderId) {
        CustomerOrder order = orderService.getOrder(orderId);
        ensureUserAccess(order.getUserId());
    }

    private void ensureUserAccess(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || !authentication.getName().equals(String.valueOf(userId))) {
            throw new AccessDeniedException("You are not allowed to access this resource");
        }
    }
}
