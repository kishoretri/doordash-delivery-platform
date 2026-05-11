package com.delivery.delivery.controller;

import com.delivery.delivery.dto.LocationUpdateRequest;
import com.delivery.delivery.repository.DeliveryRepository;
import com.delivery.delivery.service.RedisGeoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/delivery")
@Slf4j
public class DeliveryController {

    private final RedisGeoService redisGeoService;
    private final DeliveryRepository deliveryRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public DeliveryController(RedisGeoService redisGeoService, DeliveryRepository deliveryRepository, RedisTemplate<String,String> redisTemplate){
        this.redisGeoService =redisGeoService;
        this.deliveryRepository=deliveryRepository;
        this.redisTemplate=redisTemplate;
    }

    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody LocationUpdateRequest request) {
        // update location in Redis
        redisGeoService.updateDriverLocation(request.getDriverId(), request.getLongitude(), request.getLatitude());
        log.info("Driver {} location updated: {}, {}",
                request.getDriverId(), request.getLongitude(), request.getLatitude());
        return ResponseEntity.ok().build();
        // return 200 OK
    }

    @GetMapping(value = "/track/{orderId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter trackOrder(@PathVariable Long orderId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        var delivery = deliveryRepository.findByOrderId(orderId);
        if (delivery.isEmpty()) {
            // complete the emitter - no delivery found
            // log a warning
            log.warn( "connection is closing cleanly, no more data coming");
            emitter.complete();
            return emitter;
        }
        Long driverId = delivery.get().getDriverId();
        try {
            emitter.send("Driver " + driverId + " assigned to your order");
            log.info("Customer connected to track order: {}", orderId);
        } catch (Exception e) {
            emitter.complete();
        }
        return emitter;


    }
}
