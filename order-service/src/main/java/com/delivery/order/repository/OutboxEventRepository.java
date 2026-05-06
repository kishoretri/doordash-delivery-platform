package com.delivery.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.delivery.order.domain.OutboxEvent;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByProcessed(boolean processed);
}
