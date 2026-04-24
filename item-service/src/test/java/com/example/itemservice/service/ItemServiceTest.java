package com.example.itemservice.service;

import com.example.itemservice.model.Item;
import com.example.itemservice.repository.ItemRepository;
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
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void createShouldRejectNegativeInventory() {
        Item item = buildItem();
        item.setInventory(-1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> itemService.createItem(item));

        assertEquals("Inventory cannot be negative", ex.getMessage());
    }

    @Test
    void decreaseInventoryShouldUpdateRemainingCount() {
        Item item = buildItem();
        when(itemRepository.findById("item-1")).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Item updated = itemService.decreaseInventory("item-1", 3);

        assertEquals(7, updated.getInventory());
    }

    @Test
    void increaseInventoryShouldRejectNonPositiveQty() {
        Item item = buildItem();
        when(itemRepository.findById("item-1")).thenReturn(Optional.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> itemService.increaseInventory("item-1", 0));

        assertEquals("Quantity must be positive", ex.getMessage());
    }

    private Item buildItem() {
        Item item = new Item();
        item.setId("item-1");
        item.setName("Laptop");
        item.setUpc("UPC-1");
        item.setPrice(999.99);
        item.setInventory(10);
        return item;
    }
}
