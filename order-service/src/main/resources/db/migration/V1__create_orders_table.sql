CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(200) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    delivery_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE order_items (
    id            BIGSERIAL PRIMARY KEY,
    order_id      BIGINT NOT NULL REFERENCES orders(id),
    item_id       BIGINT NOT NULL,
    item_name     VARCHAR(100) NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    quantity      INTEGER NOT NULL,
    subtotal      DECIMAL(10,2) NOT NULL
);

CREATE TABLE order_outbox(
  id               BIGSERIAL PRIMARY KEY,
  aggregate_id     BIGINT NOT NULL,
  event_type       VARCHAR(255) NOT NULL,
  payload          TEXT NOT NULL,
  processed        BOOLEAN NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);