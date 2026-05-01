package com.example.itemservice.repository;

import com.example.itemservice.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {
    Optional<Item> findFirstByUpc(String upc);
}
