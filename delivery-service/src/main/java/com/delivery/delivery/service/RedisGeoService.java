package com.delivery.delivery.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RedisGeoService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisGeoService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Store driver location
    public void updateDriverLocation(Long driverId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(
                "drivers", // Redis key (the set name)
                new Point(longitude, latitude), // the location
                "driver:" + driverId // the member name
        );
    }

    // Get all available drivers near a location
    public List<String> findNearbyDrivers(double longitude, double latitude, double radiusKm) {

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().search(
                "drivers",
                new Circle(
                        new Point(longitude, latitude),
                        new Distance(radiusKm, Metrics.KILOMETERS)));

        return results.getContent().stream()
                .map(result -> result.getContent().getName())
                .collect(Collectors.toList());

    }

    // Remove driver from geo index (when they go offline)
    public void removeDriverLocation(Long driverId) {
        redisTemplate.opsForGeo().remove("drivers", "driver:" + driverId);

    }

}
