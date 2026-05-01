package com.example.orderservice.client;

import com.example.orderservice.dto.ItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "item-service-client", url = "${services.item-service-url}")
public interface ItemServiceClient {

    @GetMapping("/items/{id}")
    ItemDto getItem(@PathVariable("id") String id);

    @PostMapping("/items/{id}/decrease")
    ItemDto decreaseInventory(@PathVariable("id") String id, @RequestParam("qty") int qty);

    @PostMapping("/items/{id}/increase")
    ItemDto increaseInventory(@PathVariable("id") String id, @RequestParam("qty") int qty);
}
