CREATE KEYSPACE IF NOT EXISTS walmart_order_db
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE IF NOT EXISTS walmart_order_db.orders_by_id (
    order_id bigint PRIMARY KEY,
    user_id bigint,
    item_id text,
    quantity int,
    total_price double,
    status text,
    created_at timestamp
);

CREATE TABLE IF NOT EXISTS walmart_order_db.orders_by_user (
    user_id bigint,
    created_at timestamp,
    order_id bigint,
    item_id text,
    quantity int,
    total_price double,
    status text,
    PRIMARY KEY ((user_id), created_at, order_id)
) WITH CLUSTERING ORDER BY (created_at DESC, order_id DESC);
