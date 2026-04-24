package com.example.orderservice.controller;

import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.dto.UpdateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public CustomerOrder createOrder(@RequestParam Long userId,
                                     @RequestParam Long itemId,
                                     @RequestParam int qty) {
        return orderService.createOrder(userId, itemId, qty);
    }

    @PostMapping("/{id}/pay")
    public CustomerOrder payOrder(@PathVariable Long id) {
        return orderService.payOrder(id);
    }

    @PutMapping("/{id}")
    public CustomerOrder updateOrder(@PathVariable Long id, @RequestBody UpdateOrderRequest request) {
        return orderService.updateOrder(id, request.getQuantity());
    }

    @PostMapping("/{id}/cancel")
    public CustomerOrder cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/{id}")
    public CustomerOrder getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/user/{userId}")
    public List<CustomerOrder> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }
}
