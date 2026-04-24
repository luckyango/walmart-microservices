package com.example.itemservice.service;

import com.example.itemservice.model.Item;
import com.example.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item createItem(Item item) {
        validateItem(item);
        return itemRepository.save(item);
    }

    public List<Item> listItems() {
        return itemRepository.findAll();
    }

    public Item getItem(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    public Item decreaseInventory(Long id, int qty) {
        Item item = getItem(id);
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (item.getInventory() < qty) {
            throw new IllegalStateException("Not enough inventory");
        }
        item.setInventory(item.getInventory() - qty);
        return itemRepository.save(item);
    }

    public Item increaseInventory(Long id, int qty) {
        Item item = getItem(id);
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        item.setInventory(item.getInventory() + qty);
        return itemRepository.save(item);
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (item.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (item.getInventory() < 0) {
            throw new IllegalArgumentException("Inventory cannot be negative");
        }
    }
}
