package com.delivery.order.service;

import com.delivery.order.kafka.KafkaProducerService;
import com.delivery.order.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OutboxPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;

    public OutboxPoller(OutboxEventRepository outboxEventRepository, KafkaProducerService kafkaProducerService){
        this.outboxEventRepository=outboxEventRepository;
        this.kafkaProducerService=kafkaProducerService;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollAndPublishEvents(){
        var unprocessedEvents = outboxEventRepository.findByProcessed(false);
        for(var event : unprocessedEvents) {
            kafkaProducerService.publish(event.getEventType(), event.getPayload());
            event.setProcessed(true);
            outboxEventRepository.save(event);
        }
        if (!unprocessedEvents.isEmpty()) {
            log.info("Processed {} outbox events", unprocessedEvents.size());
        }
    }
}
