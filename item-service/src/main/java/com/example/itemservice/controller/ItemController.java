package com.example.itemservice.controller;

import com.example.itemservice.model.Item;
import com.example.itemservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemService.createItem(item);
    }

    @GetMapping
    public List<Item> listItems() {
        return itemService.listItems();
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable("id") Long id) {
        return itemService.getItem(id);
    }

    @PostMapping("/{id}/decrease")
    public Item decreaseInventory(@PathVariable("id") Long id, @RequestParam("qty") int qty) {
        return itemService.decreaseInventory(id, qty);
    }

    @PostMapping("/{id}/increase")
    public Item increaseInventory(@PathVariable("id") Long id, @RequestParam("qty") int qty) {
        return itemService.increaseInventory(id, qty);
    }
}
