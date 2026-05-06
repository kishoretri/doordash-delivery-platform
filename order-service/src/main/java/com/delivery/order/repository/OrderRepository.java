package com.delivery.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.delivery.order.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
}
