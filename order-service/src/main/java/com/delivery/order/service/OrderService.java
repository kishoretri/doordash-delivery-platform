package com.delivery.order.service;

import com.delivery.order.domain.Order;
import org.springframework.stereotype.Service;

import com.delivery.order.domain.OrderItem;
import com.delivery.order.domain.OrderStatus;
import com.delivery.order.domain.OutboxEvent;
import com.delivery.order.dto.CreateOrderRequest;
import com.delivery.order.dto.OrderResponse;
import com.delivery.order.kafka.KafkaProducerService;
import com.delivery.order.repository.OrderRepository;
import com.delivery.order.repository.OutboxEventRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;

    @Value("${app.kafka.topics.order-created}")
    private String orderCreatedTopic;

    public OrderService(OrderRepository orderRepository, OutboxEventRepository outboxEventRepository,
            KafkaProducerService kafkaProducerService) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        String orderNumber = "ORD-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();

        BigDecimal totalAmount = request.getItems().stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        // fill in the fields
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .restaurantId(request.getRestaurantId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .orderItems(orderItems)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        String payload = "{\"orderId\":" + savedOrder.getId() +
                ",\"orderNumber\":\"" + savedOrder.getOrderNumber() +
                "\",\"customerId\":" + savedOrder.getCustomerId() +
                ",\"restaurantId\":" + savedOrder.getRestaurantId() +
                ",\"totalAmount\":" + savedOrder.getTotalAmount() + "}";

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(savedOrder.getId())
                .eventType("ORDER_CREATED")
                .payload(payload)
                .processed(false)
                .build();

        outboxEventRepository.save(outboxEvent);

        log.info("Order created with number: {}", savedOrder.getOrderNumber());

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .status(savedOrder.getStatus().name())
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .build();



    }


}
