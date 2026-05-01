package com.example.orderservice.repository;

import com.example.orderservice.model.CustomerOrder;
import com.example.orderservice.repository.entity.OrderByIdEntity;
import com.example.orderservice.repository.entity.OrderByUserEntity;
import com.example.orderservice.repository.entity.OrderByUserKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class CassandraOrderRepository implements OrderRepository {
    private final CassandraTemplate cassandraTemplate;
    private final AtomicLong orderIdGenerator = new AtomicLong(System.currentTimeMillis());

    @Override
    public CustomerOrder save(CustomerOrder order) {
        if (order.getId() == null) {
            order.setId(orderIdGenerator.incrementAndGet());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        }

        cassandraTemplate.insert(toOrderByIdEntity(order));
        cassandraTemplate.insert(toOrderByUserEntity(order));
        return order;
    }

    @Override
    public Optional<CustomerOrder> findById(Long id) {
        Query query = Query.query(Criteria.where("order_id").is(id));
        OrderByIdEntity entity = cassandraTemplate.selectOne(query, OrderByIdEntity.class);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<CustomerOrder> findByUserId(Long userId) {
        Query query = Query.query(Criteria.where("user_id").is(userId));
        return cassandraTemplate.select(query, OrderByUserEntity.class)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderByIdEntity toOrderByIdEntity(CustomerOrder order) {
        OrderByIdEntity entity = new OrderByIdEntity();
        entity.setOrderId(order.getId());
        entity.setUserId(order.getUserId());
        entity.setItemId(order.getItemId());
        entity.setQuantity(order.getQuantity());
        entity.setTotalPrice(order.getTotalPrice());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(toInstant(order.getCreatedAt()));
        return entity;
    }

    private OrderByUserEntity toOrderByUserEntity(CustomerOrder order) {
        OrderByUserKey key = new OrderByUserKey();
        key.setUserId(order.getUserId());
        key.setCreatedAt(toInstant(order.getCreatedAt()));
        key.setOrderId(order.getId());

        OrderByUserEntity entity = new OrderByUserEntity();
        entity.setKey(key);
        entity.setItemId(order.getItemId());
        entity.setQuantity(order.getQuantity());
        entity.setTotalPrice(order.getTotalPrice());
        entity.setStatus(order.getStatus());
        return entity;
    }

    private CustomerOrder toDomain(OrderByIdEntity entity) {
        CustomerOrder order = new CustomerOrder();
        order.setId(entity.getOrderId());
        order.setUserId(entity.getUserId());
        order.setItemId(entity.getItemId());
        order.setQuantity(entity.getQuantity());
        order.setTotalPrice(entity.getTotalPrice());
        order.setStatus(entity.getStatus());
        order.setCreatedAt(toLocalDateTime(entity.getCreatedAt()));
        return order;
    }

    private CustomerOrder toDomain(OrderByUserEntity entity) {
        CustomerOrder order = new CustomerOrder();
        order.setId(entity.getKey().getOrderId());
        order.setUserId(entity.getKey().getUserId());
        order.setItemId(entity.getItemId());
        order.setQuantity(entity.getQuantity());
        order.setTotalPrice(entity.getTotalPrice());
        order.setStatus(entity.getStatus());
        order.setCreatedAt(toLocalDateTime(entity.getKey().getCreatedAt()));
        return order;
    }

    private Instant toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneOffset.UTC).toInstant();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneOffset.UTC).toLocalDateTime();
    }
}
