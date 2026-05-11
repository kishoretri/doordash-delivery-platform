package com.delivery.delivery.repository;

import com.delivery.delivery.domain.Driver;
import com.delivery.delivery.domain.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatus(DriverStatus status);
}
