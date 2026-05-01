package com.example.orderservice.service;

import com.example.orderservice.client.ItemServiceClient;
import com.example.orderservice.client.PaymentServiceClient;
import com.example.orderservice.dto.ItemDto;
import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemServiceClient itemServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createShouldRejectNonPositiveQty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(1L, "item-1", 0));

        assertEquals("Quantity must be positive", ex.getMessage());
    }

    @Test
    void updateShouldRecalculateTotalPrice() {
        CustomerOrder order = buildOrder();
        ItemDto item = buildItem();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemServiceClient.getItem("item-11")).thenReturn(item);
        when(itemServiceClient.decreaseInventory("item-11", 2)).thenReturn(item);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder updated = orderService.updateOrder(1L, 4);

        assertEquals(4, updated.getQuantity());
        assertEquals(80.0, updated.getTotalPrice());
    }

    @Test
    void payShouldRejectCancelledOrder() {
        CustomerOrder order = buildOrder();
        order.setStatus("CANCELLED");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderService.payOrder(1L));

        assertEquals("Only CREATED order can be paid", ex.getMessage());
    }

    @Test
    void cancelPaidOrderShouldTriggerRefundAndRestock() {
        CustomerOrder order = buildOrder();
        order.setStatus("PAID");
        ItemDto item = buildItem();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemServiceClient.increaseInventory("item-11", 2)).thenReturn(item);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder cancelled = orderService.cancelOrder(1L, "Bearer token");

        assertEquals("CANCELLED", cancelled.getStatus());
    }

    private CustomerOrder buildOrder() {
        CustomerOrder order = new CustomerOrder();
        order.setId(1L);
        order.setUserId(1L);
        order.setItemId("item-11");
        order.setQuantity(2);
        order.setTotalPrice(40.0);
        order.setStatus("CREATED");
        return order;
    }

    private ItemDto buildItem() {
        ItemDto item = new ItemDto();
        item.setId("item-11");
        item.setName("Demo item");
        item.setPrice(20.0);
        item.setInventory(20);
        return item;
    }
}
