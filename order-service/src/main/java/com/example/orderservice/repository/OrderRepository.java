package com.example.orderservice.repository;

import com.example.orderservice.model.CustomerOrder;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    CustomerOrder save(CustomerOrder order);
    Optional<CustomerOrder> findById(Long id);
    List<CustomerOrder> findByUserId(Long userId);
}
