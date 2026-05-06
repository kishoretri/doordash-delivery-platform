package com.delivery.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.delivery.order.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
