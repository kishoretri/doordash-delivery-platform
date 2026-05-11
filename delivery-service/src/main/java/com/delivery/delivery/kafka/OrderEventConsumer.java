package com.delivery.delivery.kafka;

import com.delivery.delivery.domain.Delivery;
import com.delivery.delivery.domain.DeliveryStatus;
import com.delivery.delivery.domain.DriverStatus;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.delivery.repository.DriverRepository;
import com.delivery.delivery.service.RedisGeoService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final RedisGeoService redisGeoService;

    public OrderEventConsumer(DeliveryRepository deliveryRepository, DriverRepository driverRepository,
            RedisGeoService redisGeoService) {
        this.deliveryRepository = deliveryRepository;
        this.driverRepository = driverRepository;
        this.redisGeoService = redisGeoService;
    }

    @KafkaListener(topics = "ORDER_CREATED", groupId = "delivery-service")
    public void handleOrderCreated(String message) {
        // message is the JSON payload from order-service
        // log.message
        log.info("Received order created event: {}", message);

        // payload looks like:
        // {"orderId":1,"orderNumber":"ORD-ABC123","customerId":1,"restaurantId":1,"totalAmount":14.97}
        // Simple extraction:
        String orderIdStr = message.split("\"orderId\":")[1].split(",")[0];
        Long orderId = Long.parseLong(orderIdStr);

        // We will use a fixed location for testing
        // In production this comes from restaurant-service
        List<String> nearbyDrivers = redisGeoService.findNearbyDrivers(-97.7431, 30.2672, 10.0);

        if (nearbyDrivers.isEmpty()) {
            log.warn("No drivers available for order: " + orderId);
            return;
        }

        // pick the nearest available driver
        String nearestDriver = nearbyDrivers.get(0);

        // Update driver status to BUSY
        Long driverId = Long.parseLong(nearestDriver.split(":")[1]);
        driverRepository.findById(driverId).ifPresent(driver -> {
            driver.setStatus(DriverStatus.BUSY);
            driverRepository.save(driver);
        });

        // Create a Delivery record with status ASSIGNED
        Delivery delivery = Delivery.builder()
                .orderID(orderId)
                .driverId(driverId)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();



        // Save both to database
        deliveryRepository.save(delivery);

        // Log success
        log.info("Delivery created for order {}", orderId);

    }
}
