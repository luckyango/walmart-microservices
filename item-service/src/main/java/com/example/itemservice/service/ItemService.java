package com.example.itemservice.service;

import com.example.itemservice.model.Item;
import com.example.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final MongoTemplate mongoTemplate;

    public Item createItem(Item item) {
        validateItem(item);
        if (item.getUpc() != null && !item.getUpc().isBlank()) {
            return itemRepository.findFirstByUpc(item.getUpc())
                    .map(existing -> {
                        existing.setName(item.getName());
                        existing.setPictureUrl(item.getPictureUrl());
                        existing.setPrice(item.getPrice());
                        existing.setInventory(item.getInventory());
                        return itemRepository.save(existing);
                    })
                    .orElseGet(() -> itemRepository.save(item));
        }
        return itemRepository.save(item);
    }

    public List<Item> listItems() {
        return itemRepository.findAll();
    }

    public Item getItem(String id) {
        return itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    public Item decreaseInventory(String id, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id).and("inventory").gte(qty));
        Update update = new Update().inc("inventory", -qty);

        Item updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Item.class
        );

        if (updated != null) {
            return updated;
        }
        if (!itemRepository.existsById(id)) {
            throw new IllegalArgumentException("Item not found");
        }
        throw new IllegalStateException("Not enough inventory");
    }

    public Item increaseInventory(String id, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("inventory", qty);

        Item updated = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Item.class
        );

        if (updated == null) {
            throw new IllegalArgumentException("Item not found");
        }
        return updated;
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Item name is required");
        }
        if (item.getUpc() == null || item.getUpc().isBlank()) {
            throw new IllegalArgumentException("Item UPC is required");
        }
        if (item.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (item.getInventory() < 0) {
            throw new IllegalArgumentException("Inventory cannot be negative");
        }
    }
}
