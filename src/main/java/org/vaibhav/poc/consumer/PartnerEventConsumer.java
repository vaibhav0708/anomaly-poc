package org.vaibhav.poc.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.vaibhav.poc.service.PartnerEventService;

import java.io.IOException;

@Service
@Slf4j
public class PartnerEventConsumer {

    @Autowired
    private PartnerEventService partnerEventService;

    @KafkaListener(topics = "${kafka.topic}", groupId = "anomaly-group")
    public void consumeEvent(String message) throws IOException, InterruptedException {
        log.info("Received: {}", message);

        try {
            partnerEventService.processEvent(message);
        } catch (Exception e) {
            log.error("❌ Error while processing event: {}", e.getMessage(), e);
            throw e; // rethrow so Kafka knows it failed
        }
    }
}
